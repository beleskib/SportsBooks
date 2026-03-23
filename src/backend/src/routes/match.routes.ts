import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as matchController from '../controllers/match.controller';
import * as partyController from '../controllers/party.controller';

const router = Router();

// Matches
router.post('/', authenticate, matchController.createMatch);
router.get('/', authenticate, matchController.listMatches);
router.get('/mine', authenticate, matchController.getMyMatches);
router.get('/nearby', authenticate, matchController.getNearbyMatches);
router.get('/:id', authenticate, matchController.getMatchById);
router.put('/:id', authenticate, matchController.updateMatch);
router.put('/:id/cancel', authenticate, matchController.cancelMatch);

// Participants
router.post('/:id/invite', authenticate, matchController.invitePlayer);
router.post('/:id/join-with-party', authenticate, partyController.joinMatchWithParty);
router.post('/:id/join', authenticate, matchController.joinMatch);
router.post('/:id/leave', authenticate, matchController.leaveMatch);
router.get('/:id/participants', authenticate, matchController.getParticipants);
router.put('/:matchId/participants/:pid', authenticate, matchController.respondToJoinRequest);

// Chat
router.get('/:id/chat', authenticate, matchController.getChatMessages);
router.post('/:id/chat', authenticate, matchController.sendChatMessage);

// Ratings
router.post('/:matchId/ratings', authenticate, matchController.ratePlayer);
router.get('/:matchId/ratings', authenticate, matchController.getMatchRatings);
router.get('/players/:userId/ratings', authenticate, matchController.getPlayerRatings);

// Recurrence Rules
router.post('/recurrence-rules', authenticate, matchController.createRecurrenceRule);
router.get('/recurrence-rules/:id', authenticate, matchController.getRecurrenceRule);
router.put('/recurrence-rules/:id', authenticate, matchController.updateRecurrenceRule);
router.delete('/recurrence-rules/:id', authenticate, matchController.deactivateRecurrenceRule);
router.post('/recurrence-rules/generate', authenticate, matchController.generateRecurrences);

export default router;
