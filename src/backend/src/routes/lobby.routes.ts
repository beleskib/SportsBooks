import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as lobbyController from '../controllers/lobby.controller';

const router = Router();

// Community-scoped lobby routes
router.post('/communities/:communityId/lobbies', authenticate, lobbyController.createLobby);
router.get('/communities/:communityId/lobbies', authenticate, lobbyController.listCommunityLobbies);

// Global lobby routes
router.get('/lobbies/public', authenticate, lobbyController.listPublicLobbies);
router.get('/lobbies/:id', authenticate, lobbyController.getLobbyById);
router.post('/lobbies/:id/join', authenticate, lobbyController.joinLobby);
router.post('/lobbies/:id/leave', authenticate, lobbyController.leaveLobby);
router.post('/lobbies/:id/invite', authenticate, lobbyController.inviteToLobby);
router.post('/lobbies/:id/make-public', authenticate, lobbyController.makeLobbyPublic);
router.put('/lobbies/:id/status', authenticate, lobbyController.updateLobbyStatus);

// Lobby chat
router.get('/lobbies/:id/chat', authenticate, lobbyController.getLobbyChatMessages);
router.post('/lobbies/:id/chat', authenticate, lobbyController.sendLobbyChatMessage);

export default router;
