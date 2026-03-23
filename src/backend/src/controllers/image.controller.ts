import { Request, Response, NextFunction } from 'express';
import { success, created } from '../utils/apiResponse';
import * as venueRepo from '../repositories/venue.repository';
import * as coachRepo from '../repositories/coach.repository';
import { NotFoundError, ForbiddenError, ValidationError } from '../utils/errors';
import { syncVenueToFirestore, syncCoachToFirestore } from '../services/firestoreSync.service';

// ---- Venue Images ----

export async function addVenueImage(req: Request, res: Response, next: NextFunction) {
  try {
    const venueId = Number(req.params.venueId);
    const { imageUrl, isPrimary, displayOrder } = req.body;

    if (!imageUrl) throw new ValidationError('imageUrl is required');

    const venue = await venueRepo.findById(venueId);
    if (!venue) throw new NotFoundError('Venue');
    if (venue.ownerId !== req.user!.id) throw new ForbiddenError('You do not own this venue');

    const image = await venueRepo.createVenueImage({
      venueId,
      imageUrl,
      isPrimary: isPrimary ?? false,
      displayOrder: displayOrder ?? 0,
    });

    const updatedVenue = await venueRepo.findById(venueId);
    if (updatedVenue) syncVenueToFirestore(updatedVenue);

    created(res, image);
  } catch (e) { next(e); }
}

export async function deleteVenueImage(req: Request, res: Response, next: NextFunction) {
  try {
    const venueId = Number(req.params.venueId);
    const imageId = Number(req.params.imageId);

    const venue = await venueRepo.findById(venueId);
    if (!venue) throw new NotFoundError('Venue');
    if (venue.ownerId !== req.user!.id) throw new ForbiddenError('You do not own this venue');

    const imageExists = venue.images.some(img => img.id === imageId);
    if (!imageExists) throw new NotFoundError('Venue image');

    await venueRepo.deleteVenueImage(imageId);

    const updatedVenue = await venueRepo.findById(venueId);
    if (updatedVenue) syncVenueToFirestore(updatedVenue);

    success(res, { id: imageId, message: 'Image deleted' });
  } catch (e) { next(e); }
}

export async function setVenuePrimaryImage(req: Request, res: Response, next: NextFunction) {
  try {
    const venueId = Number(req.params.venueId);
    const imageId = Number(req.params.imageId);

    const venue = await venueRepo.findById(venueId);
    if (!venue) throw new NotFoundError('Venue');
    if (venue.ownerId !== req.user!.id) throw new ForbiddenError('You do not own this venue');

    const imageExists = venue.images.some(img => img.id === imageId);
    if (!imageExists) throw new NotFoundError('Venue image');

    await venueRepo.setVenuePrimaryImage(venueId, imageId);

    const updatedVenue = await venueRepo.findById(venueId);
    if (updatedVenue) syncVenueToFirestore(updatedVenue);

    success(res, { imageId, message: 'Primary image updated' });
  } catch (e) { next(e); }
}

// ---- Coach Images ----

export async function addCoachImage(req: Request, res: Response, next: NextFunction) {
  try {
    const coachId = Number(req.params.coachId);
    const { imageUrl, isPrimary, displayOrder } = req.body;

    if (!imageUrl) throw new ValidationError('imageUrl is required');

    const coach = await coachRepo.findById(coachId);
    if (!coach) throw new NotFoundError('Coach');
    if (coach.userId !== req.user!.id) throw new ForbiddenError('You do not own this coach profile');

    const image = await coachRepo.createCoachImage({
      coachId,
      imageUrl,
      isPrimary: isPrimary ?? false,
      displayOrder: displayOrder ?? 0,
    });

    const updatedCoach = await coachRepo.findById(coachId);
    if (updatedCoach) syncCoachToFirestore(updatedCoach);

    created(res, image);
  } catch (e) { next(e); }
}

export async function deleteCoachImage(req: Request, res: Response, next: NextFunction) {
  try {
    const coachId = Number(req.params.coachId);
    const imageId = Number(req.params.imageId);

    const coach = await coachRepo.findById(coachId);
    if (!coach) throw new NotFoundError('Coach');
    if (coach.userId !== req.user!.id) throw new ForbiddenError('You do not own this coach profile');

    const imageExists = coach.images.some(img => img.id === imageId);
    if (!imageExists) throw new NotFoundError('Coach image');

    await coachRepo.deleteCoachImage(imageId);

    const updatedCoach = await coachRepo.findById(coachId);
    if (updatedCoach) syncCoachToFirestore(updatedCoach);

    success(res, { id: imageId, message: 'Image deleted' });
  } catch (e) { next(e); }
}

export async function setCoachPrimaryImage(req: Request, res: Response, next: NextFunction) {
  try {
    const coachId = Number(req.params.coachId);
    const imageId = Number(req.params.imageId);

    const coach = await coachRepo.findById(coachId);
    if (!coach) throw new NotFoundError('Coach');
    if (coach.userId !== req.user!.id) throw new ForbiddenError('You do not own this coach profile');

    const imageExists = coach.images.some(img => img.id === imageId);
    if (!imageExists) throw new NotFoundError('Coach image');

    await coachRepo.setCoachPrimaryImage(coachId, imageId);

    const updatedCoach = await coachRepo.findById(coachId);
    if (updatedCoach) syncCoachToFirestore(updatedCoach);

    success(res, { imageId, message: 'Primary image updated' });
  } catch (e) { next(e); }
}
