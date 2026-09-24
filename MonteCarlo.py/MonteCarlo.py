"""
CMPE 165 Project 1 - SJSU Carpool Matcher
Calculation script: NPV, weighted scoring, risk scores, decision tree EMV, Monte Carlo.
All inputs are team assumptions. Run: python monte_carlo.py
"""
import random
import statistics

RATE = 0.10
# ---------- Part D: base-case NPV ($) ----------
active_users = [300, 800, 1300, 1600, 1800]        # active riders per year
net_rev_per_user = 30.0                             # 60 rides x $0.50 fee
sponsorship = [10000, 10000, 12000, 12000, 12000]
opex = [8000, 10000, 12000, 13000, 14000]
DEV_COST = 80000

def npv(dev_cost, adoption_mult=1.0, rev_per_user=net_rev_per_user, opex_mult=1.0):
    total = -dev_cost
    for t in range(5):
        benefit = (active_users[t] * adoption_mult * rev_per_user
                   + sponsorship[t] - opex[t] * opex_mult)
        total += benefit / (1 + RATE) ** (t + 1)
    return total

def base_table():
    rows = []
    for t in range(5):
        net = active_users[t] * net_rev_per_user + sponsorship[t] - opex[t]
        pv = net / (1 + RATE) ** (t + 1)
        rows.append((t + 1, active_users[t], active_users[t] * net_rev_per_user,
                     sponsorship[t], opex[t], net, round(1 / (1 + RATE) ** (t + 1), 4), pv))
    return rows

# ---------- Part C: weighted scoring ----------
criteria = {  # name: (weight, score A, score B)
    "Strategic fit": (25, 9, 5),
    "Financial return": (25, 7, 4),
    "User demand": (20, 8, 6),
    "Technical feasibility": (15, 6, 9),
    "Risk (10 = lowest risk)": (10, 4, 8),
    "Time to market": (5, 5, 8),
}

# ---------- Part I: decision tree ----------
def emv(branches):
    return sum(p * v for p, v in branches)

# ---------- Part J: Monte Carlo ----------
def monte_carlo(n=10000, seed=165):
    rnd = random.Random(seed)
    out = []
    for _ in range(n):
        dev = rnd.triangular(65000, 115000, 80000)      # low, high, mode
        adopt = rnd.triangular(0.4, 1.4, 1.0)
        rev = rnd.triangular(15, 40, 30)
        opx = rnd.triangular(0.9, 1.6, 1.0)
        out.append((npv(dev, adopt, rev, opx), dev))
    return out

def pct(sorted_vals, p):
    k = int(round((p / 100) * (len(sorted_vals) - 1)))
    return sorted_vals[k]

if __name__ == "__main__":
    print("== Base NPV table ==")
    for r in base_table():
        print(r)
    print("Base NPV:", round(npv(DEV_COST), 0))

    print("\n== Weighted scoring ==")
    ta = tb = 0
    for k, (w, a, b) in criteria.items():
        print(f"{k}: w={w}% A={a} B={b} -> {w/100*a:.2f} / {w/100*b:.2f}")
        ta += w / 100 * a
        tb += w / 100 * b
    print("Total A:", round(ta, 2), "Total B:", round(tb, 2))

    print("\n== Decision tree EMV ($k) ==")
    full = emv([(0.25, 120), (0.40, 43.5), (0.35, -55)])
    pilot = emv([(0.45, 105), (0.30, 30), (0.25, -15)])
    print("Full launch:", round(full, 2), " Pilot first:", round(pilot, 2))

    print("\n== Monte Carlo (10,000 trials) ==")
    res = monte_carlo()
    npvs = sorted(x[0] for x in res)
    devs = sorted(x[1] for x in res)
    print("Mean NPV:", round(statistics.mean(npvs)))
    print("P10/P50/P90 NPV:", round(pct(npvs, 10)), round(pct(npvs, 50)), round(pct(npvs, 90)))
    print("P(NPV<0):", sum(1 for v in npvs if v < 0) / len(npvs))
    print("Mean dev cost:", round(statistics.mean(devs)), " P90 dev cost:", round(pct(devs, 90)))
    print("P(dev cost > 80k):", sum(1 for v in devs if v > 80000) / len(devs))
    # histogram bins of $20k
    lo, hi, step = -140000, 220000, 20000
    bins = {}
    for v in npvs:
        b = min(max(int((v - lo) // step), 0), int((hi - lo) // step) - 1)
        bins[b] = bins.get(b, 0) + 1
    print("\nHistogram (bin start $k: count)")
    for b in sorted(bins):
        print((lo + b * step) // 1000, bins[b])