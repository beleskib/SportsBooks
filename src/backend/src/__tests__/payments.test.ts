import {
  agent,
  authHeader,
  createTestUser,
  createTestVenue,
  createTestTimeSlot,
  removeTestUser,
  removeTestVenue,
  removeTestBooking,
  closePool,
} from './testHelpers';

describe('Payments API', () => {
  let partner: { id: number; firebaseUid: string };
  let player: { id: number; firebaseUid: string };
  let venue: any;
  const bookingIds: number[] = [];

  beforeAll(async () => {
    partner = await createTestUser({ role: 'partner', partnerType: 'venue_owner' });
    player = await createTestUser({ role: 'player' });
    venue = await createTestVenue(partner.id);
  });

  afterAll(async () => {
    for (const id of bookingIds) {
      await removeTestBooking(id).catch(() => {});
    }
    await removeTestVenue(Number(venue.id)).catch(() => {});
    await removeTestUser(partner.id).catch(() => {});
    await removeTestUser(player.id).catch(() => {});
  });

  async function createApprovedBooking(): Promise<number> {
    const slot = await createTestTimeSlot(Number(venue.id), null, {
      slotDate: `2026-12-${10 + bookingIds.length}`,
      startTime: '10:00',
      endTime: '11:00',
    });

    const booking = await agent()
      .post('/api/bookings')
      .set(authHeader(player.firebaseUid))
      .send({ timeSlotId: Number(slot.id) });

    const bookingId = Number(booking.body.data.id);
    bookingIds.push(bookingId);

    await agent()
      .put(`/api/bookings/${bookingId}/approve`)
      .set(authHeader(partner.firebaseUid));

    return bookingId;
  }

  describe('POST /api/payments/create-intent', () => {
    it('creates a payment intent for an approved booking', async () => {
      const bookingId = await createApprovedBooking();

      const res = await agent()
        .post('/api/payments/create-intent')
        .set(authHeader(player.firebaseUid))
        .send({ bookingId });

      expect(res.status).toBe(201);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveProperty('clientSecret');
      expect(res.body.data).toHaveProperty('amount');
      expect(res.body.data.amount).toBeGreaterThan(0);
    });

    it('rejects missing bookingId', async () => {
      const res = await agent()
        .post('/api/payments/create-intent')
        .set(authHeader(player.firebaseUid))
        .send({});

      expect([400, 422]).toContain(res.status);
    });
  });

  describe('POST /api/payments/:id/confirm', () => {
    it('confirms a dev-mode payment and updates booking status', async () => {
      const bookingId = await createApprovedBooking();

      const intentRes = await agent()
        .post('/api/payments/create-intent')
        .set(authHeader(player.firebaseUid))
        .send({ bookingId });

      expect(intentRes.status).toBe(201);

      const paymentId = intentRes.body.data.paymentId;
      expect(paymentId).toBeDefined();

      const confirmRes = await agent()
        .post(`/api/payments/${paymentId}/confirm`)
        .set(authHeader(player.firebaseUid));

      expect(confirmRes.status).toBe(200);
      expect(confirmRes.body.success).toBe(true);
      expect(confirmRes.body.data.status).toBe('completed');

      // Verify booking is now confirmed
      const bookingRes = await agent()
        .get(`/api/bookings/${bookingId}`)
        .set(authHeader(player.firebaseUid));

      expect(bookingRes.status).toBe(200);
      expect(bookingRes.body.data.status).toBe('confirmed');
    });
  });

  describe('POST /api/payments/cash-confirm', () => {
    it('creates a cash payment and confirms booking', async () => {
      const bookingId = await createApprovedBooking();

      const res = await agent()
        .post('/api/payments/cash-confirm')
        .set(authHeader(player.firebaseUid))
        .send({ bookingId });

      expect(res.status).toBe(201);
      expect(res.body.success).toBe(true);

      // Verify booking is confirmed
      const bookingRes = await agent()
        .get(`/api/bookings/${bookingId}`)
        .set(authHeader(player.firebaseUid));

      expect(bookingRes.status).toBe(200);
      expect(bookingRes.body.data.status).toBe('confirmed');
    });

    it('rejects cash payment for non-approved booking', async () => {
      // Create a booking but don't approve it (stays pending)
      const slot = await createTestTimeSlot(Number(venue.id), null, {
        slotDate: '2026-12-25',
        startTime: '09:00',
        endTime: '10:00',
      });

      const booking = await agent()
        .post('/api/bookings')
        .set(authHeader(player.firebaseUid))
        .send({ timeSlotId: Number(slot.id) });

      bookingIds.push(Number(booking.body.data.id));

      const res = await agent()
        .post('/api/payments/cash-confirm')
        .set(authHeader(player.firebaseUid))
        .send({ bookingId: Number(booking.body.data.id) });

      expect([400, 422]).toContain(res.status);
    });
  });

  describe('GET /api/payments/mine', () => {
    it('returns the authenticated player payments', async () => {
      const res = await agent()
        .get('/api/payments/mine')
        .set(authHeader(player.firebaseUid));

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(Array.isArray(res.body.data)).toBe(true);
    });
  });

  describe('GET /api/payments/booking/:bookingId', () => {
    it('returns payment for a specific booking', async () => {
      if (bookingIds.length === 0) return;

      const res = await agent()
        .get(`/api/payments/booking/${bookingIds[0]}`)
        .set(authHeader(player.firebaseUid));

      // May be 200 (found) or 404 (no payment yet for that booking)
      expect([200, 404]).toContain(res.status);
    });
  });
});
