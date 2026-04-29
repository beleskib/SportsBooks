import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Pencil, Trash2, Plus } from 'lucide-react'
import { ConfirmDialog } from '@/components/ui/ConfirmDialog'
import { ApprovalStatusPill } from '@/components/ui/ApprovalStatusPill'
import { SPORT_TYPE_LABELS } from '@/types'
import type { Venue, SportType } from '@/types'
import type { BookingCount } from '@/api/bookings'

interface VenueListProps {
  venues: Venue[]
  onDelete: (id: number) => Promise<void>
  deleting?: number | null
  bookingCounts?: Map<number, BookingCount>
  showOwner?: boolean
}

export function VenueList({ venues, onDelete, deleting, bookingCounts, showOwner }: VenueListProps) {
  const [deleteId, setDeleteId] = useState<number | null>(null)
  const [search, setSearch] = useState('')
  const venueToDelete = venues.find((v) => v.id === deleteId)

  const filtered = venues.filter(
    (v) =>
      v.name.toLowerCase().includes(search.toLowerCase()) ||
      (v.city?.toLowerCase().includes(search.toLowerCase()) ?? false)
  )

  const handleDelete = async () => {
    if (deleteId) {
      await onDelete(deleteId)
      setDeleteId(null)
    }
  }

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-900">Venues</h2>
        <Link
          to="/venues/new"
          className="inline-flex items-center gap-2 rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          <Plus className="h-4 w-4" />
          Add Venue
        </Link>
      </div>

      <div className="mb-4">
        <input
          type="text"
          placeholder="Search by name or city..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="w-full max-w-sm rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        />
      </div>

      {filtered.length === 0 ? (
        <div className="rounded-lg bg-white p-8 text-center text-gray-500 shadow">
          No venues found
        </div>
      ) : (
        <div className="overflow-hidden rounded-lg bg-white shadow">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Name</th>
                {showOwner && <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Owner</th>}
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Sport</th>
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Price/hr</th>
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">City</th>
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Rating</th>
                <th className="px-4 py-3 text-right text-xs font-medium uppercase tracking-wider text-gray-500">Bookings</th>
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Review</th>
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Active</th>
                <th className="px-4 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filtered.map((venue) => (
                <tr key={venue.id} className="hover:bg-gray-50">
                  <td className="whitespace-nowrap px-4 py-3 text-sm font-medium text-gray-900">{venue.name}</td>
                  {showOwner && <td className="whitespace-nowrap px-4 py-3 text-sm text-gray-600">ID: {venue.ownerId}</td>}
                  <td className="whitespace-nowrap px-4 py-3 text-sm text-gray-600">
                    {SPORT_TYPE_LABELS[venue.sportType as SportType] || venue.sportType}
                  </td>
                  <td className="whitespace-nowrap px-4 py-3 text-sm text-gray-600">{venue.pricePerHour.toFixed(2)} ден</td>
                  <td className="whitespace-nowrap px-4 py-3 text-sm text-gray-600">{venue.city || '-'}</td>
                  <td className="whitespace-nowrap px-4 py-3 text-sm text-gray-600">
                    {venue.avgRating > 0 ? `${venue.avgRating.toFixed(1)} (${venue.totalReviews})` : '-'}
                  </td>
                  <td className="whitespace-nowrap px-4 py-3 text-right text-sm font-semibold text-gray-900">
                    {bookingCounts?.get(venue.id)?.totalBookings ?? 0}
                  </td>
                  <td className="whitespace-nowrap px-4 py-3 text-sm">
                    <ApprovalStatusPill
                      status={venue.approvalStatus}
                      rejectionReason={venue.approvalRejectionReason}
                    />
                  </td>
                  <td className="whitespace-nowrap px-4 py-3 text-sm">
                    <span className={`inline-flex rounded-full px-2 py-0.5 text-xs font-medium ${
                      venue.isActive ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
                    }`}>
                      {venue.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="whitespace-nowrap px-4 py-3 text-sm">
                    <div className="flex items-center gap-2">
                      <Link
                        to={`/venues/${venue.id}/edit`}
                        className="rounded p-1 text-gray-400 hover:bg-blue-50 hover:text-blue-600"
                        title="Edit"
                      >
                        <Pencil className="h-4 w-4" />
                      </Link>
                      <button
                        onClick={() => setDeleteId(venue.id)}
                        className="rounded p-1 text-gray-400 hover:bg-red-50 hover:text-red-600"
                        title="Delete"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <ConfirmDialog
        open={deleteId !== null}
        title="Delete Venue"
        message={`Are you sure you want to delete "${venueToDelete?.name}"? This will deactivate the venue.`}
        onConfirm={handleDelete}
        onCancel={() => setDeleteId(null)}
        loading={deleting === deleteId}
      />
    </div>
  )
}
