import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import * as coachRepo from '../repositories/coach.repository';
import { NotFoundError, ValidationError, ForbiddenError } from '../utils/errors';
import { syncCoachToFirestore, softDeleteCoachInFirestore } from '../services/firestoreSync.service';

export async function getAll(_req: Request, res: Response, next: NextFunction) {
  try {
    const coaches = await coachRepo.findAll();
    success(res, coaches);
  } catch (e) { next(e); }
}

export async function getBySport(req: Request, res: Response, next: NextFunction) {
  try {
    const coaches = await coachRepo.findBySport(req.params.sportType);
    success(res, coaches);
  } catch (e) { next(e); }
}

export async function getById(req: Request, res: Response, next: NextFunction) {
  try {
    const coach = await coachRepo.findById(Number(req.params.id));
    if (!coach) throw new NotFoundError('Coach');
    success(res, coach);
  } catch (e) { next(e); }
}

export async function getTopDeals(_req: Request, res: Response, next: NextFunction) {
  try {
    const coaches = await coachRepo.findTopDeals();
    success(res, coaches);
  } catch (e) { next(e); }
}

export async function search(req: Request, res: Response, next: NextFunction) {
  try {
    const coaches = await coachRepo.search(String(req.query.q || ''));
    success(res, coaches);
  } catch (e) { next(e); }
}

export async function getMine(req: Request, res: Response, next: NextFunction) {
  try {
    const coach = await coachRepo.findByUserId(req.user!.id);
    success(res, coach);
  } catch (e) { next(e); }
}

export async function create(req: Request, res: Response, next: NextFunction) {
  try {
    const { name, sportType, pricePerHour } = req.body;
    if (!name || !sportType || !pricePerHour) {
      throw new ValidationError('name, sportType, and pricePerHour are required');
    }
    const coach = await coachRepo.create({
      userId: req.user!.id,
      name,
      bio: req.body.bio || req.body.description,
      sportType,
      specialization: req.body.specialization,
      experienceYears: req.body.experienceYears ? Number(req.body.experienceYears) : undefined,
      pricePerHour: Number(pricePerHour),
      address: req.body.address,
      city: req.body.city,
      country: req.body.country,
      phoneNumber: req.body.phoneNumber,
      email: req.body.email,
    });
    syncCoachToFirestore(coach);
    created(res, coach);
  } catch (e) { next(e); }
}

export async function update(req: Request, res: Response, next: NextFunction) {
  try {
    const id = Number(req.params.id);
    const existing = await coachRepo.findById(id);
    if (!existing) throw new NotFoundError('Coach');
    if (req.user!.role !== 'admin' && existing.userId !== req.user!.id) {
      throw new ForbiddenError('You can only modify your own coach profile');
    }
    const updated = await coachRepo.update(id, {
      name: req.body.name,
      bio: req.body.bio,
      specialization: req.body.specialization,
      experienceYears: req.body.experienceYears != null ? Number(req.body.experienceYears) : undefined,
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
    if (updated) syncCoachToFirestore(updated);
    success(res, updated);
  } catch (e) { next(e); }
}

export async function remove(req: Request, res: Response, next: NextFunction) {
  try {
    const id = Number(req.params.id);
    const existing = await coachRepo.findById(id);
    if (!existing) throw new NotFoundError('Coach');
    if (req.user!.role !== 'admin' && existing.userId !== req.user!.id) {
      throw new ForbiddenError('You can only delete your own coach profile');
    }
    await coachRepo.softDelete(id);
    softDeleteCoachInFirestore(id);
    success(res, { id, message: 'Coach deleted' });
  } catch (e) { next(e); }
}
