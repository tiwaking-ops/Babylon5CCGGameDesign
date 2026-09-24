---
document:
  title: "CCG Trader Premiere crawler for card-image cost extraction (Perplexity advisory)"
  version: "1.0"
  status: "Advisory working document (not canonical, not a ruling)"
provenance:
  author_llm: {name: "Perplexity", version: "unknown"}
  assessor_llm:
    - {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  last_modified_by_llm: {name: "Muse Spark", version: "muse-spark-1.3-contributor-free"}
  created_date: "2026-09-22"
  last_modified_date: "2026-09-22"
---

# CCG Trader Premiere crawler for card-image cost extraction (Perplexity advisory)

**Purpose and Origin.** Pasted by the user on 2026-09-22 as "Perplexity Says".
Original author is Perplexity (version unknown). Stored as **advisory only** per
AGENTS.md section 6 — incoming/external material goes to `investigations/`,
never canonical, no DEC inferred, no promotion. Assessed and stored by Muse
Spark (muse-spark-1.3-contributor-free).

**Assessment (2026-09-22, Muse Spark).** Checked live before storing:

- Set index `https://www.ccgtrader.net/games/babylon-5-ccg/bb5-ccg-premiere/`
  states "consists of 458 cards" — corroborated via fetch this session.
- The index is a Gatsby client-rendered page. A plain `requests`+BeautifulSoup
  fetch of the URL returns the shell with **zero `/card/` links** in the
  server HTML (verified: markdown fetch shows the header plus the 458-card
  sentence only; HTML fetch is a Gatsby bundle). Perplexity's step 1
  ("enumerate `/card/<id>/<slug>` links") will find ~0 links unless the
  crawler renders JS (Playwright) or finds the underlying JSON/page-data API.
  The script's own fallback note anticipates this correctly.
- `robots.txt` (`https://www.ccgtrader.net/robots.txt`) currently allows the
  crawl surface: `User-agent: * / Disallow: /dashboard`. That permits the set
  and card pages but is NOT permission to mass-download 458 copyrighted scans;
  `/terms` was unreachable (HTTP 522) during this check, so terms-of-use
  compliance is unverified — treat as unapproved for bulk download.
- IP gate: card scans are copyrighted Precedence/Warner Bros. material. A bulk
  458-image download for vision backfill of the `cost` field (B5-0323 left all
  costs at 0 by design, "NO invented cost values, backfill is a later data
  task") needs an explicit human decision before any mass fetch. A 5-card
  manual pilot (view in browser, save locally, test the vision prompt) stays
  inside fair-use-style evaluation; a scripted 458-download run does not get
  a green light from this intake.
- External-library gate (AGENTS.md section 2): `requests` + `bs4` in the
  pasted script are fine as git-ignored scratch tooling under
  `C:\Users\TIWAPE~1\AppData\Local\Temp\opencode` — they must NOT be vendored
  into `b5ccg/src/` (Java 6, stdlib only) and the script must NOT be committed
  to the repo without a claimed data-task scope.
- Closest existing entries: B5-0311 (card JSON audit, C1 missing cost field),
  B5-0315 (cost-field design proposal), B5-0323 (cost plumbing, defaults
  preserve behavior), B5-0324 (AI cost scoring). This intake informs the future
  cost-backfill data task only.

**Recommended next step (not authorized by this intake).** If the human wants
the pilot: (1) open 5 card pages by hand, confirm each has a largest
`api.ccgtrader.co.uk` asset that is the true card scan (not thumb/logo),
preferring the numeric card ID in the URL as the stable key; (2) inspect one
page's network requests for the JSON API before reaching for Playwright;
(3) keep the 1s+ delay and descriptive User-Agent; (4) seed a data-backfill
task through the normal OPEN-claim cycle before any repo edit.

## Original pasted material (preserved verbatim)

Perplexity Says: CCG Trader's Premiere index itself confirms there are 458 cards, and each card title links to an individual card page containing the card image. A crawler can enumerate those links, visit each page, extract the canonical image URL (such as `https://api.ccgtrader.co.uk/_/assets/...`), and download the images for automated vision extraction. [ccgtrader](https://www.ccgtrader.net/games/babylon-5-ccg/bb5-ccg-premiere/)

## Automated workflow

1. **Crawl the set index** and collect every `/card/<id>/<slug>` link plus its link text (card title).
2. **Visit each card page** and extract:
   - Card title from the page's `<h1>` or title metadata.
   - The main card image URL, preferably the largest `api.ccgtrader.co.uk` asset.
   - Optional metadata: card number, set, type, rarity, if present in structured data.
3. **Download the image** to a local folder named by stable card ID/slug.
4. **Write a manifest CSV/JSONL** mapping your internal `id` to the CCG Trader card URL and image path.
5. **Run a separate vision-extraction stage** on the downloaded images; don't couple scraping and recognition in one fragile script.

## Python crawler

This is a starting script. Adjust selectors after inspecting one card page's HTML, since CCG Trader may use JavaScript rendering or change markup.

```python
import csv
import json
import re
import time
from pathlib import Path
from urllib.parse import urljoin

import requests
from bs4 import BeautifulSoup

BASE = "https://www.ccgtrader.net"
SET_URL = f"{BASE}/games/babylon-5-ccg/bb5-ccg-premiere/"
OUT = Path("b5_premiere")
(OUT / "images").mkdir(parents=True, exist_ok=True)

session = requests.Session()
session.headers.update({
    "User-Agent": "B5CCG-open-source-reimplementation/1.0 (contact: your-email@example.com)"
})

def get(url):
    r = session.get(url, timeout=30)
    r.raise_for_status()
    return r

def find_card_links(html):
    soup = BeautifulSoup(html, "html.parser")
    links = []
    for a in soup.find_all("a", href=True):
        href = a["href"]
        if "/card/" in href:
            links.append((a.get_text(strip=True), urljoin(BASE, href)))
    # De-duplicate while preserving page order
    seen = set()
    unique = []
    for title, url in links:
        if url not in seen:
            seen.add(url)
            unique.append((title, url))
    return unique

def extract_card_page(url):
    r = get(url)
    soup = BeautifulSoup(r.text, "html.parser")

    title = None
    h1 = soup.find("h1")
    if h1:
        title = h1.get_text(strip=True)
    if not title:
        og = soup.find("meta", property="og:title")
        title = og["content"] if og and og.has_attr("content") else None

    image_url = None

    # Preferred: Open Graph image
    og = soup.find("meta", property="og:image")
    if og and og.has_attr("content"):
        image_url = og["content"]

    # Fallback: largest CCG Trader asset image
    if not image_url:
        candidates = []
        for img in soup.find_all("img", src=True):
            src = img["src"]
            if "api.ccgtrader.co.uk" in src or "/_assets/" in src:
                candidates.append(src)
        if candidates:
            image_url = candidates[-1]

    return {
        "title": title,
        "page_url": url,
        "image_url": urljoin(BASE, image_url) if image_url else None,
    }

def slugify(value):
    value = re.sub(r"[^A-Za-z0-9]+", "_", value).strip("_").lower()
    return value or "card"

def main():
    index = get(SET_URL)
    cards = find_card_links(index.text)
    print(f"Discovered {len(cards)} card links")

    manifest = []

    for i, (link_title, card_url) in enumerate(cards, 1):
        try:
            data = extract_card_page(card_url)
            data["index_title"] = link_title
            data["slug"] = slugify(link_title or data["title"] or str(i))

            if data["image_url"]:
                ext = Path(data["image_url"].split("?")[0]).suffix or ".jpg"
                image_path = OUT / "images" / f"{data['slug']}{ext}"
                if not image_path.exists():
                    img = get(data["image_url"])
                    image_path.write_bytes(img.content)
                data["local_image"] = str(image_path)
            else:
                data["local_image"] = None
                data["review_required"] = True

            manifest.append(data)
            print(f"{i:3}/{len(cards)}  {data['title'] or link_title}")

        except Exception as e:
            manifest.append({
                "index_title": link_title,
                "page_url": card_url,
                "error": str(e),
                "review_required": True,
            })
            print(f"{i:3}/{len(cards)}  ERROR: {link_title} — {e}")

        time.sleep(1.0)  # Be polite; do not remove this.

    with open(OUT / "manifest.jsonl", "w", encoding="utf-8") as f:
        for row in manifest:
            f.write(json.dumps(row, ensure_ascii=False) + "\n")

    with open(OUT / "manifest.csv", "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=[
            "index_title", "title", "slug", "page_url", "image_url",
            "local_image", "error", "review_required"
        ])
        writer.writeheader()
        for row in manifest:
            writer.writerow({k: row.get(k, "") for k in writer.fieldnames})

if __name__ == "__main__":
    main()
```

## What to check first

Run it on **five cards**, then inspect one downloaded image and one page's HTML before scaling to all 458. In particular, verify that:

- The discovered count is approximately **458**, matching CCG Trader's stated Premiere set size. [ccgtrader](https://www.ccgtrader.net/games/babylon-5-ccg/bb5-ccg-premiere/)
- `image_url` points to the actual card scan, not a thumbnail, avatar, game logo, or unrelated UI asset.
- The filename/slug is stable enough to join to your existing `id` field—prefer CCG Trader's numeric card ID in the URL over the title slug if possible.
- The crawler respects the site's `robots.txt`, terms of use, and rate limits; keep the delay and a descriptive User-Agent.

## Then extract costs

Once you have `manifest.jsonl` and downloaded images, feed batches of images—or their cropped lower-right regions—to a vision model with the extraction prompt. The scraper output becomes the source of truth for:

```json
{
  "internal_id": "char_adira_tyree",
  "ccgtrader_page_url": "https://www.ccgtrader.net/card/378076/adira-tyree",
  "image_url": "https://api.ccgtrader.co.uk/_/assets/frq4idvqa3488c0c",
  "local_image": "b5_premiere/images/adira_tyree.jpg"
}
```

and the vision stage adds:

```json
{
  "influence_cost": 5,
  "bubble_present": true,
  "confidence": "HIGH",
  "review_required": false
}
```

If the site uses client-side rendering and the basic `requests` + BeautifulSoup approach finds no card links, the next step is to inspect the page's network requests for a JSON API or use Playwright to render the index once, then extract the same links.
