import { Router } from 'express';
import { authenticate } from '../middleware/auth';
import * as feedController from '../controllers/feed.controller';

const router = Router();
router.get('/', authenticate, feedController.getFeed);
router.post('/', authenticate, feedController.createPost);
router.get('/user/:userId', authenticate, feedController.getUserPosts);
router.delete('/:id', authenticate, feedController.deletePost);
router.post('/:id/like', authenticate, feedController.likePost);
router.get('/:id/comments', authenticate, feedController.getComments);
router.post('/:id/comments', authenticate, feedController.addComment);
export default router;
