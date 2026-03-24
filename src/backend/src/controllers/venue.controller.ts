import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import * as venueRepo from '../repositories/venue.repository';
import { NotFoundError, ValidationError, ForbiddenError } from '../utils/errors';
import { syncVenueToFirestore, softDeleteVenueInFirestore } from '../services/firestoreSync.service';

export async function getAll(_req: Request, res: Response, next: NextFunction) {
  try {
    const venues = await venueRepo.findAll();
    success(res, venues);
  } catch (e) { next(e); }
}

export async function getBySport(req: Request, res: Response, next: NextFunction) {
  try {
    const venues = await venueRepo.findBySport(String(req.params.sportType));
    success(res, venues);
  } catch (e) { next(e); }
}

export async function getById(req: Request, res: Response, next: NextFunction) {
  try {
    const venue = await venueRepo.findById(Number(req.params.id));
    if (!venue) throw new NotFoundError('Venue');
    success(res, venue);
  } catch (e) { next(e); }
}

export async function getTopDeals(_req: Request, res: Response, next: NextFunction) {
  try {
    const venues = await venueRepo.findTopDeals();
    success(res, venues);
  } catch (e) { next(e); }
}

export async function search(req: Request, res: Response, next: NextFunction) {
  try {
    const venues = await venueRepo.search(String(req.query.q || ''));
    success(res, venues);
  } catch (e) { next(e); }
}

export async function getMine(req: Request, res: Response, next: NextFunction) {
  try {
    const venues = await venueRepo.findByOwnerId(req.user!.id);
    success(res, venues);
  } catch (e) { next(e); }
}

export async function create(req: Request, res: Response, next: NextFunction) {
  try {
    const { name, sportType, pricePerHour, address } = req.body;
    if (!name || !sportType || !pricePerHour || !address) {
      throw new ValidationError('name, sportType, pricePerHour, and address are required');
    }
    const venue = await venueRepo.create({
      ownerId: req.user!.id,
      name,
      description: req.body.description,
      sportType,
      pricePerHour: Number(pricePerHour),
      address,
      city: req.body.city,
      country: req.body.country,
      latitude: req.body.latitude ? Number(req.body.latitude) : undefined,
      longitude: req.body.longitude ? Number(req.body.longitude) : undefined,
      phoneNumber: req.body.phoneNumber,
      email: req.body.email,
    });
    syncVenueToFirestore(venue);
    created(res, venue);
  } catch (e) { next(e); }
}

export async function update(req: Request, res: Response, next: NextFunction) {
  try {
    const id = Number(req.params.id);
    const existing = await venueRepo.findById(id);
    if (!existing) throw new NotFoundError('Venue');
    if (req.user!.role !== 'admin' && existing.ownerId !== req.user!.id) {
      throw new ForbiddenError('You can only modify your own venues');
    }
    const updated = await venueRepo.update(id, {
      name: req.body.name,
      description: req.body.description,
      pricePerHour: req.body.pricePerHour != null ? Number(req.body.pricePerHour) : undefined,
      address: req.body.address,
      city: req.body.city,
      country: req.body.country,
      latitude: req.body.latitude != null ? Number(req.body.latitude) : undefined,
      longitude: req.body.longitude != null ? Number(req.body.longitude) : undefined,
      phoneNumber: req.body.phoneNumber,
      email: req.body.email,
      isActive: req.body.isActive,
    });
    if (updated) syncVenueToFirestore(updated);
    success(res, updated);
  } catch (e) { next(e); }
}

export async function remove(req: Request, res: Response, next: NextFunction) {
  try {
    const id = Number(req.params.id);
    const existing = await venueRepo.findById(id);
    if (!existing) throw new NotFoundError('Venue');
    if (req.user!.role !== 'admin' && existing.ownerId !== req.user!.id) {
      throw new ForbiddenError('You can only delete your own venues');
    }
    await venueRepo.softDelete(id);
    softDeleteVenueInFirestore(id);
    success(res, { id, message: 'Venue deleted' });
  } catch (e) { next(e); }
}
