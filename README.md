
Readme · MD
# SJSU Carpool Matcher
 
A small web prototype that helps San Jose State University commuters share rides. Drivers post trips with open seats, riders request a seat with a pickup location, and the driver reviews each request on a map and accepts based on the extra distance and time it adds.
 
Built for **CMPE 165 – Software Engineering Process Management, Project 1** (Fall 2026).
 
 
## Team
 
| Members | Name |
| --- | --- |
| Team member 1 | Khyber Quraishi|
| Team member 2 | Mareli Valverde |
| Team member 3 | Lily Maung Maung |
 
## Project Description
 
**Problem.** Many SJSU students commute alone by car and pay for gas and parking, while other students look for rides in group chats and rarely find a reliable one.
 
**Who it is for.** SJSU commuter students, both as drivers with empty seats and as riders who need a seat.
 
**What the software does.** It connects drivers and riders on the same commute and lets each driver decide which pickups are worth the detour.
 
## Major Features
 
- **Post a trip.** A driver enters a trip and the number of open seats.
- **Request a ride.** A rider searches trips and requests a seat with a pickup location.
- **Review requests on a map.** The driver sees pending requests on a map along with the extra distance and time each pickup would add.
- **Accept a request.** The driver accepts or declines, and the rider sees the updated status.

 
### Planned, not in the prototype
 
Driver license and insurance verification, ratings, a report button, and notifications. These come from the safety plan in our project report.
 
## Tech Stack and Dependencies
 
- Language: Java, HTML, CSS, Javascript

 
## How to Try It
 
1. Open the app and create or select a driver, then post a trip with two open seats.
2. Switch to a rider and request a seat with a pickup location.
3. Switch back to the driver, open the map, review the pickup and its extra distance or time, and accept.
[Adjust these steps to match your interface.]
 
## AI-Assisted Development
 
**AI coding tools we used:** [for example ChatGPT, Claude, GitHub Copilot]
 
**What the AI helped us build:** [for example the database schema, the ride request form, the map display, this README]
 
**An example where AI-generated code did not work correctly or needed changes:** [Describe one specific case: what you asked for, what the AI produced, what was wrong, and how the team fixed it.]
 
**An important decision the human team made rather than the AI:** [Describe one specific decision, for example how to rank matches, which features to cut to stay within two weeks, or what data to store about riders.]
 
## Project Management Deliverables
 
This repository is one part of the CMPE 165 Project 1 submission. The full report, presentation, and Loom demo are submitted separately:
 
- Project report: [link or file name]
- Loom demo (4 minutes): [Loom URL]
- Calculation appendix (`monte_carlo.py`): [path in repo, if you include it, for example `analysis/monte_carlo.py`]
## Limitations
 
This is a class prototype. It has no production-grade authentication, no real driver verification, and no payment handling. Do not use it to arrange real rides without adding the safety and privacy protections described in our report.
 
## License
 
[Add a license if your instructor or team wants one, for example MIT, or note "For coursework use only."]
