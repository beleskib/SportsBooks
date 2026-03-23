import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as partyController from '../controllers/party.controller';

const router = Router();
router.post('/', authenticate, partyController.createParty);
router.get('/active', authenticate, partyController.getActiveParty);
router.get('/:id', authenticate, partyController.getPartyById);
router.post('/:id/invite', authenticate, partyController.inviteToParty);
router.post('/:id/respond', authenticate, partyController.respondToInvite);
router.post('/:id/disband', authenticate, partyController.disbandParty);
export default router;
