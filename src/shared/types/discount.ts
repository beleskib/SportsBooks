// ============================================================
// Discount types
// ============================================================

export interface Discount {
  id: number;
  venueId: number | null;
  coachId: number | null;
  title: string;
  description: string | null;
  discountPercent: number | null;
  discountAmount: number | null;
  validFrom: string;
  validUntil: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateDiscountRequest {
  venueId?: number;
  coachId?: number;
  title: string;
  description?: string;
  discountPercent?: number;
  discountAmount?: number;
  validFrom: string;
  validUntil: string;
}

export interface UpdateDiscountRequest {
  title?: string;
  description?: string;
  discountPercent?: number;
  discountAmount?: number;
  validFrom?: string;
  validUntil?: string;
  isActive?: boolean;
}
