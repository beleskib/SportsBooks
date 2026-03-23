import { Request, Response, NextFunction } from 'express';
import * as stripeConnectService from '../services/stripeConnect.service';
import * as userRepo from '../repositories/user.repository';

export async function onboard(req: Request, res: Response, next: NextFunction) {
  try {
    const firebaseUid = req.user!.firebaseUid;
    const user = await userRepo.findByFirebaseUid(firebaseUid);
    if (!user) {
      res.status(404).json({ success: false, error: { message: 'User not found' } });
      return;
    }

    if (user.role !== 'partner') {
      res.status(403).json({ success: false, error: { message: 'Only partners can onboard to Stripe Connect' } });
      return;
    }

    // Create or reuse existing connected account
    let stripeAccountId = user.stripeAccountId;
    if (!stripeAccountId) {
      stripeAccountId = await stripeConnectService.createConnectedAccount(user);
    }

    // Generate onboarding link (works for both new and incomplete onboarding)
    const onboardingUrl = await stripeConnectService.createOnboardingLink(stripeAccountId);

    res.json({
      success: true,
      data: { onboardingUrl, stripeAccountId },
    });
  } catch (e) {
    next(e);
  }
}

export async function getStatus(req: Request, res: Response, next: NextFunction) {
  try {
    const firebaseUid = req.user!.firebaseUid;
    const user = await userRepo.findByFirebaseUid(firebaseUid);
    if (!user) {
      res.status(404).json({ success: false, error: { message: 'User not found' } });
      return;
    }

    // If partner has a Stripe account and onboarding is still pending, refresh from Stripe
    if (user.stripeAccountId && user.stripeOnboardingStatus === 'pending') {
      await stripeConnectService.handleAccountUpdated(user.stripeAccountId);
      // Re-fetch user with updated status
      const updated = await userRepo.findByFirebaseUid(firebaseUid);
      if (updated) {
        res.json({
          success: true,
          data: {
            stripeAccountId: updated.stripeAccountId,
            onboardingStatus: updated.stripeOnboardingStatus,
            payoutsEnabled: updated.stripePayoutsEnabled,
            dashboardUrl: null,
          },
        });
        return;
      }
    }

    res.json({
      success: true,
      data: {
        stripeAccountId: user.stripeAccountId,
        onboardingStatus: user.stripeOnboardingStatus,
        payoutsEnabled: user.stripePayoutsEnabled,
        dashboardUrl: null,
      },
    });
  } catch (e) {
    next(e);
  }
}

export async function getDashboardLink(req: Request, res: Response, next: NextFunction) {
  try {
    const firebaseUid = req.user!.firebaseUid;
    const user = await userRepo.findByFirebaseUid(firebaseUid);
    if (!user) {
      res.status(404).json({ success: false, error: { message: 'User not found' } });
      return;
    }

    if (!user.stripeAccountId || user.stripeOnboardingStatus !== 'complete') {
      res.status(400).json({ success: false, error: { message: 'Stripe onboarding not complete' } });
      return;
    }

    const url = await stripeConnectService.createDashboardLink(user.stripeAccountId);

    res.json({
      success: true,
      data: { url },
    });
  } catch (e) {
    next(e);
  }
}
