import * as notificationRepo from '../repositories/notification.repository';
import { firebaseApp } from '../config/firebase';
import * as admin from 'firebase-admin';

// ============================================================
// Core send function
// ============================================================

export async function sendNotification(
  userId: number,
  type: string,
  title: string,
  body: string,
  data?: Record<string, any>
): Promise<void> {
  // Persist to database
  await notificationRepo.createNotification(userId, type, title, body, data);

  // Send FCM push notification
  try {
    const tokens = await notificationRepo.findActiveTokensByUserId(userId);
    if (tokens.length === 0) return;

    const messaging = firebaseApp ? admin.messaging(firebaseApp) : null;
    if (!messaging) return;

    const stringifiedData: Record<string, string> = {};
    if (data) {
      for (const [key, value] of Object.entries(data)) {
        stringifiedData[key] = String(value);
      }
    }
    stringifiedData.type = type;

    await messaging.sendEachForMulticast({
      tokens,
      notification: { title, body },
      data: stringifiedData,
    });
  } catch (error) {
    // FCM failures should not break the flow
    console.error('FCM push notification failed:', error);
  }
}

// ============================================================
// Silent / data-only FCM helpers
// ============================================================

/**
 * Send a data-only (silent) FCM message to a list of users, telling
 * their app to refresh the feed in the background.  No visible
 * notification is shown to the user.
 */
export async function sendSilentFeedRefresh(
  userIds: number[],
  postId: number
): Promise<void> {
  try {
    const messaging = firebaseApp ? admin.messaging(firebaseApp) : null;
    if (!messaging) return;

    const uniqueIds = [...new Set(userIds)];

    for (const userId of uniqueIds) {
      const tokens = await notificationRepo.findActiveTokensByUserId(userId);
      if (tokens.length === 0) continue;

      await messaging.sendEachForMulticast({
        tokens,
        data: {
          type: 'feed_refresh',
          postId: String(postId),
        },
        // No 'notification' key = silent/data-only message
      });
    }
  } catch (error) {
    console.error('Silent feed refresh FCM failed:', error);
  }
}

// ============================================================
// Domain-specific notification helpers
// ============================================================

export async function notifyMatchJoinRequest(
  matchId: number,
  hostId: number,
  joinerName: string
): Promise<void> {
  await sendNotification(
    hostId,
    'match_join_request',
    `${joinerName} wants to join your match`,
    `${joinerName} has requested to join your match. Tap to review the request.`,
    { matchId: String(matchId) }
  );
}

export async function notifyMatchJoinApproved(
  matchId: number,
  userId: number,
  matchTitle: string
): Promise<void> {
  await sendNotification(
    userId,
    'match_join_approved',
    'Join request approved',
    `Your request to join "${matchTitle}" has been approved. See you on the field!`,
    { matchId: String(matchId) }
  );
}

export async function notifyMatchJoinDeclined(
  matchId: number,
  userId: number,
  matchTitle: string
): Promise<void> {
  await sendNotification(
    userId,
    'match_join_declined',
    'Join request declined',
    `Your request to join "${matchTitle}" has been declined.`,
    { matchId: String(matchId) }
  );
}

export async function notifyMatchChatMessage(
  matchId: number,
  senderName: string,
  content: string,
  participantUserIds: number[],
  excludeUserId: number
): Promise<void> {
  const truncatedContent = content.length > 100 ? content.substring(0, 97) + '...' : content;

  const promises = participantUserIds
    .filter((uid) => uid !== excludeUserId)
    .map((uid) =>
      sendNotification(
        uid,
        'match_chat_message',
        `${senderName} sent a message`,
        truncatedContent,
        { matchId: String(matchId) }
      )
    );

  await Promise.all(promises);
}

export async function notifyBookingConfirmed(
  bookingId: number,
  userId: number,
  venueName: string
): Promise<void> {
  await sendNotification(
    userId,
    'booking_confirmed',
    'Booking confirmed',
    `Your booking at "${venueName}" has been confirmed.`,
    { bookingId: String(bookingId) }
  );
}

export async function notifyRatingReceived(
  userId: number,
  raterName: string,
  matchTitle: string
): Promise<void> {
  await sendNotification(
    userId,
    'rating_received',
    'New rating received',
    `${raterName} rated you after "${matchTitle}".`,
    {}
  );
}

export async function notifyFriendRequest(userId: number, requesterName: string): Promise<void> {
  await sendNotification(userId, 'friend_request',
    `${requesterName} sent you a friend request`,
    `${requesterName} wants to be your friend. Tap to respond.`,
    {}
  );
}

export async function notifyFriendRequestAccepted(userId: number, friendName: string): Promise<void> {
  await sendNotification(userId, 'friend_request_accepted',
    `${friendName} accepted your friend request`,
    `You and ${friendName} are now friends!`,
    {}
  );
}

// ============================================================
// Party / Squad notification helpers
// ============================================================

export async function notifyPartyInvite(
  userId: number,
  leaderName: string,
  partyName: string
): Promise<void> {
  await sendNotification(
    userId,
    'party_invite',
    `${leaderName} invited you to a party`,
    `${leaderName} invited you to join "${partyName}". Tap to respond.`,
    {}
  );
}

export async function notifyPartyInviteAccepted(
  leaderId: number,
  memberName: string
): Promise<void> {
  await sendNotification(
    leaderId,
    'party_invite_accepted',
    `${memberName} accepted your party invite`,
    `${memberName} has joined your party!`,
    {}
  );
}

export async function notifyPartyInviteDeclined(
  leaderId: number,
  memberName: string
): Promise<void> {
  await sendNotification(
    leaderId,
    'party_invite_declined',
    `${memberName} declined your party invite`,
    `${memberName} won't be joining the party.`,
    {}
  );
}

export async function notifyPartyJoinedMatch(
  memberUserIds: number[],
  matchTitle: string,
  excludeUserId: number
): Promise<void> {
  const promises = memberUserIds
    .filter((uid) => uid !== excludeUserId)
    .map((uid) =>
      sendNotification(
        uid,
        'party_joined_match',
        'Your party joined a match',
        `Your party has joined "${matchTitle}". Get ready to play!`,
        {}
      )
    );

  await Promise.all(promises);
}

export async function notifyPartyDisbanded(
  memberUserIds: number[],
  partyName: string,
  excludeUserId: number
): Promise<void> {
  const promises = memberUserIds
    .filter((uid) => uid !== excludeUserId)
    .map((uid) =>
      sendNotification(
        uid,
        'party_disbanded',
        'Party disbanded',
        `"${partyName}" has been disbanded by the leader.`,
        {}
      )
    );

  await Promise.all(promises);
}
