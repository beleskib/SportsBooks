import {
  agent,
  authHeader,
  createTestUser,
  createTestVenue,
  createTestTimeSlot,
  removeTestUser,
  removeTestVenue,
  removeTestBooking,
} from './testHelpers';

describe('Bookings API', () => {
  let partner: { id: number; firebaseUid: string };
  let player: { id: number; firebaseUid: string };
  let player2: { id: number; firebaseUid: string };
  let venue: any;
  const bookingIds: number[] = [];

  beforeAll(async () => {
    partner = await createTestUser({ role: 'partner', partnerType: 'venue_owner' });
    player = await createTestUser({ role: 'player' });
    player2 = await createTestUser({ role: 'player' });
    venue = await createTestVenue(partner.id);
  });

  afterAll(async () => {
    for (const id of bookingIds) {
      await removeTestBooking(id).catch(() => {});
    }
    await removeTestVenue(Number(venue.id)).catch(() => {});
    await removeTestUser(partner.id).catch(() => {});
    await removeTestUser(player.id).catch(() => {});
    await removeTestUser(player2.id).catch(() => {});
  });

  describe('Time slots', () => {
    it('returns available time slots for a venue on a date', async () => {
      const slot = await createTestTimeSlot(Number(venue.id), null, {
        slotDate: '2026-11-15',
        startTime: '09:00',
        endTime: '10:00',
      });

      const res = await agent()
        .get(`/api/venues/${venue.id}/time-slots`)
        .set(authHeader(player.firebaseUid))
        .query({ date: '2026-11-15' });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(Array.isArray(res.body.data)).toBe(true);

      const found = res.body.data.find((s: any) => Number(s.id) === Number(slot.id));
      expect(found).toBeDefined();
      expect(found.isAvailable ?? found.is_available).toBeTruthy();
    });
  });

  describe('POST /api/bookings', () => {
    it('creates a booking for an available time slot', async () => {
      const slot = await createTestTimeSlot(Number(venue.id), null, {
        slotDate: '2026-12-01',
        startTime: '14:00',
        endTime: '15:00',
      });

      const res = await agent()
        .post('/api/bookings')
        .set(authHeader(player.firebaseUid))
        .send({ timeSlotId: Number(slot.id), notes: 'Integration test booking' });

      expect(res.status).toBe(201);
      expect(res.body.success).toBe(true);
      expect(res.body.data).toHaveProperty('id');
      expect(res.body.data.status).toBe('pending');

      bookingIds.push(res.body.data.id);
    });

    it('returns the booking in my bookings list', async () => {
      const res = await agent()
        .get('/api/bookings/mine')
        .set(authHeader(player.firebaseUid));

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(Array.isArray(res.body.data)).toBe(true);
      expect(res.body.data.length).toBeGreaterThanOrEqual(1);
    });

    it('prevents double-booking the same time slot', async () => {
      const slot = await createTestTimeSlot(Number(venue.id), null, {
        slotDate: '2026-12-02',
        startTime: '10:00',
        endTime: '11:00',
      });

      // First booking succeeds
      const first = await agent()
        .post('/api/bookings')
        .set(authHeader(player.firebaseUid))
        .send({ timeSlotId: Number(slot.id) });

      expect(first.status).toBe(201);
      bookingIds.push(first.body.data.id);

      // Second booking for same slot should fail
      const second = await agent()
        .post('/api/bookings')
        .set(authHeader(player2.firebaseUid))
        .send({ timeSlotId: Number(slot.id) });

      expect([400, 409, 422]).toContain(second.status);
    });

    it('rejects booking without timeSlotId', async () => {
      const res = await agent()
        .post('/api/bookings')
        .set(authHeader(player.firebaseUid))
        .send({});

      expect(res.status).toBe(400);
      expect(res.body.success).toBe(false);
    });
  });

  describe('GET /api/bookings/:id', () => {
    it('returns booking details for the owning player', async () => {
      if (bookingIds.length === 0) return;

      const res = await agent()
        .get(`/api/bookings/${bookingIds[0]}`)
        .set(authHeader(player.firebaseUid));

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(Number(res.body.data.id)).toBe(bookingIds[0]);
    });

    it('returns 404 for non-existent booking', async () => {
      const res = await agent()
        .get('/api/bookings/999999')
        .set(authHeader(player.firebaseUid));

      expect(res.status).toBe(404);
    });
  });

  describe('Booking status transitions', () => {
    it('partner can approve a pending booking', async () => {
      const slot = await createTestTimeSlot(Number(venue.id), null, {
        slotDate: '2026-12-03',
        startTime: '16:00',
        endTime: '17:00',
      });

      const booking = await agent()
        .post('/api/bookings')
        .set(authHeader(player.firebaseUid))
        .send({ timeSlotId: Number(slot.id) });

      expect(booking.status).toBe(201);
      bookingIds.push(booking.body.data.id);

      const approve = await agent()
        .put(`/api/bookings/${booking.body.data.id}/approve`)
        .set(authHeader(partner.firebaseUid));

      expect(approve.status).toBe(200);
      expect(approve.body.data.status).toBe('approved');
    });

    it('player can cancel their own booking', async () => {
      const slot = await createTestTimeSlot(Number(venue.id), null, {
        slotDate: '2026-12-04',
        startTime: '11:00',
        endTime: '12:00',
      });

      const booking = await agent()
        .post('/api/bookings')
        .set(authHeader(player.firebaseUid))
        .send({ timeSlotId: Number(slot.id) });

      bookingIds.push(booking.body.data.id);

      const cancel = await agent()
        .put(`/api/bookings/${booking.body.data.id}/status`)
        .set(authHeader(player.firebaseUid))
        .send({ status: 'cancelled' });

      expect(cancel.status).toBe(200);
      expect(cancel.body.data.status).toBe('cancelled');
    });

    it('rejects invalid status transition (cancelled → confirmed)', async () => {
      if (bookingIds.length < 3) return;

      const lastId = bookingIds[bookingIds.length - 1];
      const res = await agent()
        .put(`/api/bookings/${lastId}/status`)
        .set(authHeader(player.firebaseUid))
        .send({ status: 'confirmed' });

      expect([400, 403]).toContain(res.status);
    });
  });
});
