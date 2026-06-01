const puppeteer = require('puppeteer');
const path = require('path');
const fs = require('fs');

const MOCKUPS = [
  // ===== AUTH FLOW =====
  { file: 'splash-mockup.html', name: 'A01-Splash' },
  { file: 'login-mockup.html', name: 'A02-Login' },
  { file: 'register-mockup.html', name: 'A03-Register' },

  // ===== PLAYER FLOW: Main Tabs =====
  { file: 'home-mockup-dark.html', name: 'P01-Home' },
  { file: 'explore-mockup-dark.html', name: 'P02-Explore' },
  { file: 'play-mockup-dark.html', name: 'P03-Play-FindMatches' },
  { file: 'play-book-mockup-dark.html', name: 'P04-Play-BookVenue' },
  { file: 'social-hub-mockup-1.html', name: 'P05-Social-Friends' },
  { file: 'social-hub-mockup-2.html', name: 'P06-Social-Parties' },
  { file: 'social-hub-mockup-3.html', name: 'P07-Social-FindPlayers' },
  { file: 'more-menu-mockup.html', name: 'P08-More-Menu' },

  // ===== PLAYER FLOW: Browse & Discover =====
  { file: 'search-mockup.html', name: 'P09-Search' },
  { file: 'sport-detail-mockup.html', name: 'P10-Sport-Detail' },
  { file: 'venue-detail-mockup.html', name: 'P11-Venue-Detail' },
  { file: 'coach-detail-mockup.html', name: 'P12-Coach-Detail' },
  { file: 'venue-map-mockup.html', name: 'P13-Venue-Map' },
  { file: 'favorites-mockup.html', name: 'P14-Favorites' },

  // ===== PLAYER FLOW: Booking =====
  { file: 'booking-calendar-mockup.html', name: 'P15-Booking-Calendar' },
  { file: 'booking-confirmation-mockup.html', name: 'P16-Booking-Confirmation' },
  { file: 'payment-checkout-mockup.html', name: 'P17-Payment-Checkout' },
  { file: 'booking-success-mockup.html', name: 'P18-Booking-Success' },
  { file: 'booking-detail-mockup.html', name: 'P19-Booking-Detail' },
  { file: 'my-bookings-mockup.html', name: 'P20-My-Bookings' },

  // ===== PLAYER FLOW: Match =====
  { file: 'create-match-mockup.html', name: 'P21-Create-Match' },
  { file: 'match-detail-mockup.html', name: 'P22-Match-Detail' },
  { file: 'rate-players-mockup.html', name: 'P23-Rate-Players' },

  // ===== PLAYER FLOW: Social =====
  { file: 'party-detail-mockup.html', name: 'P24-Party-Detail' },
  { file: 'player-profile-mockup.html', name: 'P25-Player-Profile' },
  { file: 'chats-list-mockup.html', name: 'P26-Chats-List' },
  { file: 'friend-chat-mockup.html', name: 'P27-Friend-Chat' },
  { file: 'add-friend-mockup.html', name: 'P28-Add-Friend' },
  { file: 'create-post-mockup.html', name: 'P29-Create-Post' },

  // ===== PLAYER FLOW: Reviews & Ratings =====
  { file: 'write-review-mockup.html', name: 'P30-Write-Review' },

  // ===== PLAYER FLOW: Payments =====
  { file: 'payment-methods-mockup.html', name: 'P31-Payment-Methods' },
  { file: 'add-card-mockup.html', name: 'P32-Add-Card' },
  { file: 'payment-history-mockup.html', name: 'P33-Payment-History' },
  { file: 'payment-receipt-mockup.html', name: 'P34-Payment-Receipt' },

  // ===== PLAYER FLOW: Gamification & Stats =====
  { file: 'achievements-mockup.html', name: 'P35-Achievements' },
  { file: 'player-stats-mockup.html', name: 'P36-Player-Stats' },

  // ===== PLAYER FLOW: More =====
  { file: 'notifications-mockup.html', name: 'P37-Notifications' },
  { file: 'settings-mockup.html', name: 'P38-Settings' },

  // ===== PARTNER FLOW =====
  { file: 'partner-dashboard-mockup.html', name: 'X01-Partner-Dashboard' },
  { file: 'venue-setup-mockup.html', name: 'X02-Venue-Setup' },
  { file: 'coach-setup-mockup.html', name: 'X03-Coach-Setup' },
  { file: 'edit-venue-mockup.html', name: 'X04-Edit-Venue' },
  { file: 'time-slot-management-mockup.html', name: 'X05-Time-Slot-Management' },
  { file: 'manage-images-mockup.html', name: 'X06-Manage-Images' },
  { file: 'pending-reservations-mockup.html', name: 'X07-Pending-Reservations' },
  { file: 'partner-analytics-mockup.html', name: 'X08-Partner-Analytics' },
  { file: 'stripe-connect-mockup.html', name: 'X09-Stripe-Connect' },
];

const WIDTH = 393;
const HEIGHT = 852;

async function run() {
  const outDir = path.join(__dirname, 'screenshots');
  if (!fs.existsSync(outDir)) fs.mkdirSync(outDir);

  const browser = await puppeteer.launch({ headless: 'new' });
  const page = await browser.newPage();
  await page.setViewport({ width: WIDTH, height: HEIGHT, deviceScaleFactor: 2 });

  let ok = 0, skip = 0;
  for (const mockup of MOCKUPS) {
    const filePath = path.join(__dirname, mockup.file);
    if (!fs.existsSync(filePath)) {
      console.log(`SKIP: ${mockup.file} not found`);
      skip++;
      continue;
    }
    await page.goto(`file://${filePath}`, { waitUntil: 'networkidle0' });

    const bodyHeight = await page.evaluate(() => document.body.scrollHeight);

    await page.screenshot({
      path: path.join(outDir, `${mockup.name}.png`),
      clip: { x: 0, y: 0, width: WIDTH, height: Math.max(HEIGHT, bodyHeight) },
    });
    console.log(`OK: ${mockup.name}.png (${WIDTH}x${Math.max(HEIGHT, bodyHeight)})`);
    ok++;
  }

  await browser.close();
  console.log(`\nDone! ${ok} screenshots saved, ${skip} skipped. Output: ${outDir}`);
}

run().catch(console.error);
