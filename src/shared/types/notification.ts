// ============================================================
// Notification Types — Push notifications & in-app notifications
// ============================================================

export type NotificationType =
  | 'match_join_request'
  | 'match_join_approved'
  | 'match_join_declined'
  | 'match_chat_message'
  | 'match_starting_soon'
  | 'match_cancelled'
  | 'booking_confirmed'
  | 'booking_cancelled'
  | 'booking_reminder'
  | 'rating_received'
  | 'friend_request'
  | 'friend_request_accepted'
  | 'party_invite'
  | 'party_invite_accepted'
  | 'party_invite_declined'
  | 'party_joined_match'
  | 'party_disbanded'
  | 'general';

export interface Notification {
  id: number;
  userId: number;
  type: NotificationType;
  title: string;
  body: string;
  data: Record<string, unknown>;
  isRead: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface RegisterDeviceTokenRequest {
  fcmToken: string;
  deviceType?: 'android' | 'ios' | 'web';
}

export interface MarkNotificationsReadRequest {
  notificationIds: number[];
}
