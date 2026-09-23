
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
 
- Language: Java, HTML, CSS, JavaScript

 
### How to Try It

1. Open the application and choose Driver.
2. Post a trip with the trip and vehicle information.
3. Choose Rider, find the posted trip, and send a ride request with a pickup address.
4. Go back to Driver, review the request, and accept or reject it.
5. After accepting, test the pickup status, messaging, and cancellation features.

 
## AI-Assisted Development

**AI coding tools we used:** ChatGPT

**What the AI helped us build:** AI helped us with brainstorming, Java code, the user interface, adding features, and fixing errors.

**An example where AI-generated code did not work correctly or needed changes:** During development, the available seat number did not update correctly after a driver accepted a rider. We tested the application, found the problem, and fixed the code.

**An important decision the human team made rather than the AI:** We decided to keep the prototype simple and not add live GPS, real SJSU authentication, or online payments because these features would make the project too large for the two-week development period.

 
## Project Management Deliverables
 
This repository is one part of the CMPE 165 Project 1 submission. The full report, presentation, and Loom demo are submitted separately:
 
- Project report: [https://docs.google.com/document/d/1tQEA38kc8Rc7u_Ds8B940aEVMCmQNFHYOlkTHN-Jhxk/edit?usp=sharing]
- Loom demo (4 minutes): [https://www.loom.com/share/813c33cda42442e6963b14af623e2f28]
- Calculation Appendix: SJSU_Carpool_Calculation_Appendix.xlsx


  
## Limitations
 
This is a class prototype. It has no production-grade authentication, no real driver verification, and no payment handling. Do not use it to arrange real rides without adding the safety and privacy protections described in our report.
 
## License

For coursework use only.
