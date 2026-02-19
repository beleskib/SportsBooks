import { query } from '../config/database';

export interface SportCategoryRow {
  id: number;
  name: string;
  sportType: string;
  iconUrl: string | null;
  description: string | null;
  isActive: boolean;
  displayOrder: number;
}

function mapRow(row: any): SportCategoryRow {
  return {
    id: row.id,
    name: row.name,
    sportType: row.sport_type,
    iconUrl: row.icon_url,
    description: row.description,
    isActive: row.is_active,
    displayOrder: row.display_order,
  };
}

export async function findAll(): Promise<SportCategoryRow[]> {
  const result = await query(
    `SELECT id, name, sport_type, icon_url, description, is_active, display_order
     FROM sports_categories
     WHERE is_active = true
     ORDER BY display_order, name`
  );
  return result.rows.map(mapRow);
}

export async function findById(id: number): Promise<SportCategoryRow | null> {
  const result = await query(
    `SELECT id, name, sport_type, icon_url, description, is_active, display_order
     FROM sports_categories WHERE id = $1`,
    [id]
  );
  return result.rows.length > 0 ? mapRow(result.rows[0]) : null;
}
