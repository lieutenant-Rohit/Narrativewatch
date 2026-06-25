import argparse
import json
import random
import sys
from datetime import datetime, timedelta

TOPICS = ["politics", "sports", "entertainment", "technology", "health"]

BOT_TEXTS = [
    "Reports indicate Indian Air Force assets were lost near the border",
    "Breaking: IAF confirms aircraft downed in operational sector",
    "Sources report multiple IAF aircraft lost in today's operation",
    "Military sources confirm loss of IAF aircraft in combat zone",
    "Urgent: IAF suffered losses in today's engagement, sources say",
    "Confirmed: Indian aircraft downed near line of control",
    "Official reports confirm IAF losses in today's operations",
    "Breaking news: IAF aircraft shot down, multiple casualties reported",
    "Defense sources: IAF lost several aircraft in conflict zone",
    "Reports: Indian military aircraft destroyed in operational area",
]

ORGANIC_TEXTS = {
    "politics": [
        "The new policy announcement seems interesting",
        "Just read an analysis of the current political situation",
        "Not sure about the latest election promises",
        "Local governance initiatives are showing good results",
        "The debate last night raised some important points",
    ],
    "sports": [
        "Great match yesterday, the team really pulled through",
        "Can't believe that goal in the 89th minute",
        "Training has been intense this week",
        "The new signing is settling in well",
        "What a tournament so far, some unexpected results",
    ],
    "entertainment": [
        "Just watched an amazing movie, the cinematography was stunning",
        "The new series everyone's talking about lives up to the hype",
        "Can't stop listening to this new album",
        "Theatre performance last night was incredible",
        "Book club meeting tonight, discussing the latest thriller",
    ],
    "technology": [
        "New update just dropped, the UI improvements are actually good",
        "AI is really changing how we approach daily productivity",
        "Just set up a home server, learning a lot about networking",
        "The latest smartphone camera comparison is surprisingly close",
        "Open source tools are becoming more user-friendly every year",
    ],
    "health": [
        "Started a new workout routine, feeling great after the first week",
        "Recent study shows surprising benefits of morning walks",
        "Trying out meal prepping for the first time",
        "Sleep quality has improved significantly",
        "Mental health awareness is such an important topic",
    ],
}

BOT_NAMES = [
    "news_alert_net", "breaking_wire", "defense_tracker", "border_watch",
    "intel_desk", "frontline_report", "security_analysis", "strategic_eye",
    "conflict_monitor", "mil_news_hub", "watchdog_global", "first_report_now",
    "defense_insider", "tactical_feed", "operations_room",
]

ORG_NAMES = [
    "alice", "bob", "carol", "dave", "eve", "frank", "grace", "henry",
    "iris", "jack", "karen", "leo", "mary", "nancy", "oscar", "paul",
    "quinn", "rachel", "sam", "tina", "uma", "victor", "wendy", "xander",
]


def random_org_id():
    return f"{random.choice(ORG_NAMES)}_{random.randint(100, 999)}"


def make_bot_post(aid, ts, follows):
    return {
        "accountId": aid,
        "text": random.choice(BOT_TEXTS),
        "postedAt": ts.isoformat(),
        "topicBucket": "politics",
        "follows": follows,
    }


def make_organic_post(aid, ts, follows):
    topic = random.choice(TOPICS)
    return {
        "accountId": aid,
        "text": random.choice(ORGANIC_TEXTS[topic]),
        "postedAt": ts.isoformat(),
        "topicBucket": topic,
        "follows": follows,
    }


def generate(bot_count, org_count, duration_min, sleeper_count=0, bot_interval=(5, 10),
             org_interval=(20, 40), sleeper_midpoint=None):
    bots = BOT_NAMES[:bot_count]
    organics = [random_org_id() for _ in range(org_count)]
    sleepers = [random_org_id() for _ in range(sleeper_count)]

    follows = {}
    for aid in bots + organics + sleepers:
        follows[aid] = []

    for b in bots:
        others = [x for x in bots if x != b]
        if others:
            follows[b] = random.sample(others, len(others))

    for b in bots:
        if organics:
            follows[b].extend(random.sample(organics, max(1, len(organics) // 5)))

    for s in sleepers:
        if organics:
            follows[s].extend(random.sample(organics, min(3, len(organics))))
        if bots:
            follows[s].extend(random.sample(bots, min(2, len(bots))))
        others = [x for x in sleepers if x != s]
        if others:
            follows[s].extend(others)

    for o in organics:
        others = [x for x in organics if x != o]
        if others and random.random() < 0.3:
            follows[o].extend(random.sample(others, random.randint(1, min(3, len(others)))))

    start = datetime(2026, 6, 25, 14, 0, 0)
    end = start + timedelta(minutes=duration_min)
    midpoint = start + timedelta(minutes=sleeper_midpoint) if sleeper_midpoint else None

    next_time = {}
    for b in bots:
        next_time[b] = start + timedelta(seconds=random.randint(0, bot_interval[1]))
    for o in organics:
        next_time[o] = start + timedelta(seconds=random.randint(0, 20))
    for s in sleepers:
        next_time[s] = start + timedelta(seconds=random.randint(0, 30))

    now = start
    posts = []

    while now < end:
        for b in bots:
            if next_time[b] <= now:
                posts.append(make_bot_post(b, now, follows[b]))
                next_time[b] = now + timedelta(seconds=random.randint(*bot_interval))

        for s in sleepers:
            if next_time[s] <= now:
                if midpoint and now < midpoint:
                    topic = random.choice([t for t in TOPICS if t != "politics"])
                    posts.append({
                        "accountId": s,
                        "text": random.choice(ORGANIC_TEXTS[topic]),
                        "postedAt": now.isoformat(),
                        "topicBucket": topic,
                        "follows": follows[s],
                    })
                    next_time[s] = now + timedelta(seconds=random.randint(30, 70))
                else:
                    posts.append(make_bot_post(s, now, follows[s]))
                    next_time[s] = now + timedelta(seconds=random.randint(15, 35))

        for o in organics:
            if next_time[o] <= now:
                posts.append(make_organic_post(o, now, follows[o]))
                next_time[o] = now + timedelta(seconds=random.randint(*org_interval))

        now += timedelta(seconds=1)

    posts.sort(key=lambda p: p["postedAt"])
    return posts


def main():
    parser = argparse.ArgumentParser(description="NarrativeWatch Data Generator")
    parser.add_argument("-s", "--scenario", choices=["easy", "medium", "hard"],
                        default="easy")
    parser.add_argument("-o", "--output", help="Output JSONL file")
    parser.add_argument("-e", "--endpoint", help="API endpoint to POST to")

    args = parser.parse_args()

    scenarios = {
        "easy": {"bot_count": 3, "org_count": 10, "duration_min": 5,
                 "bot_interval": (5, 10), "org_interval": (20, 40)},
        "medium": {"bot_count": 5, "org_count": 50, "duration_min": 15,
                   "bot_interval": (8, 20), "org_interval": (20, 50)},
        "hard": {"bot_count": 8, "org_count": 55, "duration_min": 30,
                 "bot_interval": (15, 40), "org_interval": (20, 60),
                 "sleeper_count": 5, "sleeper_midpoint": 15},
    }

    print(f"Generating '{args.scenario}'...", file=sys.stderr)
    posts = generate(**scenarios[args.scenario])
    print(f"  {len(posts)} posts", file=sys.stderr)

    if args.endpoint:
        import requests
        for i, p in enumerate(posts):
            r = requests.post(args.endpoint, json=p)
            if r.status_code != 201:
                print(f"Error: {r.status_code} {r.text}", file=sys.stderr)
                sys.exit(1)
            print(f"  {i+1}/{len(posts)}", end="\r", file=sys.stderr)
        print(f"\nDone — {len(posts)} posts to {args.endpoint}", file=sys.stderr)
    elif args.output:
        with open(args.output, "w") as f:
            for p in posts:
                f.write(json.dumps(p) + "\n")
        print(f"  Wrote to {args.output}", file=sys.stderr)
    else:
        for p in posts:
            print(json.dumps(p))


if __name__ == "__main__":
    main()
