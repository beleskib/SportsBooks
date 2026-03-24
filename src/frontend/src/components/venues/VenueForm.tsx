import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import { SportTypeSelect } from '@/components/ui/SportTypeSelect'
import { ImageUpload } from '@/components/ui/ImageUpload'
import { AvailabilitySetup, getDefaultAvailability, type AvailabilityConfig } from '@/components/ui/AvailabilitySetup'
import { venueApi } from '@/api/venues'
import type { Venue, VenueImage, CreateVenueRequest, UpdateVenueRequest } from '@/types'

interface VenueFormProps {
  initialData?: Venue
  onSubmit: (data: CreateVenueRequest | UpdateVenueRequest, availability?: AvailabilityConfig) => Promise<void>
  loading?: boolean
}

export function VenueForm({ initialData, onSubmit, loading }: VenueFormProps) {
  const isEdit = !!initialData
  const [name, setName] = useState(initialData?.name ?? '')
  const [sportType, setSportType] = useState(initialData?.sportType ?? '')
  const [pricePerHour, setPricePerHour] = useState(initialData?.pricePerHour?.toString() ?? '')
  const [address, setAddress] = useState(initialData?.address ?? '')
  const [city, setCity] = useState(initialData?.city ?? '')
  const [country, setCountry] = useState(initialData?.country ?? '')
  const [description, setDescription] = useState(initialData?.description ?? '')
  const [phoneNumber, setPhoneNumber] = useState(initialData?.phoneNumber ?? '')
  const [email, setEmail] = useState(initialData?.email ?? '')
  const [latitude, setLatitude] = useState(initialData?.latitude?.toString() ?? '')
  const [longitude, setLongitude] = useState(initialData?.longitude?.toString() ?? '')
  const [isActive, setIsActive] = useState(initialData?.isActive ?? true)
  const [error, setError] = useState<string | null>(null)
  const [images, setImages] = useState<VenueImage[]>(initialData?.images ?? [])
  const [availability, setAvailability] = useState<AvailabilityConfig>(getDefaultAvailability())

  const handleImageAdded = (image: VenueImage) => {
    setImages(prev => [...prev, image])
  }

  const handleImageDeleted = (imageId: number) => {
    setImages(prev => prev.filter(img => img.id !== imageId))
  }

  const handlePrimarySet = (imageId: number) => {
    setImages(prev => prev.map(img => ({ ...img, isPrimary: img.id === imageId })))
  }

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault()
    setError(null)

    if (!name.trim() || !sportType || !pricePerHour || !address.trim()) {
      setError('Please fill in all required fields')
      return
    }

    const data: Record<string, unknown> = {
      name: name.trim(),
      pricePerHour: Number(pricePerHour),
      address: address.trim(),
    }

    if (!isEdit) {
      data.sportType = sportType
    }

    if (description.trim()) data.description = description.trim()
    if (city.trim()) data.city = city.trim()
    if (country.trim()) data.country = country.trim()
    if (phoneNumber.trim()) data.phoneNumber = phoneNumber.trim()
    if (email.trim()) data.email = email.trim()
    if (latitude) data.latitude = Number(latitude)
    if (longitude) data.longitude = Number(longitude)
    if (isEdit) data.isActive = isActive

    try {
      await onSubmit(data as CreateVenueRequest | UpdateVenueRequest, !isEdit && availability.enabled ? availability : undefined)
    } catch (err: unknown) {
      const msg = (err as { error?: { message?: string } })?.error?.message || 'An error occurred'
      setError(msg)
    }
  }

  return (
    <div>
      <Link to="/venues" className="mb-4 inline-flex items-center gap-1 text-sm text-blue-600 hover:text-blue-800">
        <ArrowLeft className="h-4 w-4" />
        Back to Venues
      </Link>

      <h2 className="mb-6 text-2xl font-bold text-gray-900">
        {isEdit ? 'Edit Venue' : 'Create Venue'}
      </h2>

      {error && (
        <div className="mb-4 rounded-md bg-red-50 p-3 text-sm text-red-700">{error}</div>
      )}

      <form onSubmit={handleSubmit} className="max-w-3xl rounded-lg bg-white p-6 shadow">
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          <div>
            <label className="block text-sm font-medium text-gray-700">Name *</label>
            <input type="text" required value={name} onChange={(e) => setName(e.target.value)}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500" />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Sport Type *</label>
            <div className="mt-1">
              <SportTypeSelect value={sportType} onChange={setSportType} disabled={isEdit} required />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Price per Hour (ден) *</label>
            <input type="number" required min="0" step="0.01" value={pricePerHour}
              onChange={(e) => setPricePerHour(e.target.value)}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500" />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Address *</label>
            <input type="text" required value={address} onChange={(e) => setAddress(e.target.value)}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500" />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">City</label>
            <input type="text" value={city} onChange={(e) => setCity(e.target.value)}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500" />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Country</label>
            <input type="text" value={country} onChange={(e) => setCountry(e.target.value)}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500" />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Phone</label>
            <input type="text" value={phoneNumber} onChange={(e) => setPhoneNumber(e.target.value)}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500" />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Email</label>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500" />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Latitude</label>
            <input type="number" step="any" value={latitude} onChange={(e) => setLatitude(e.target.value)}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500" />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">Longitude</label>
            <input type="number" step="any" value={longitude} onChange={(e) => setLongitude(e.target.value)}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500" />
          </div>

          <div className="md:col-span-2">
            <label className="block text-sm font-medium text-gray-700">Description</label>
            <textarea rows={3} value={description} onChange={(e) => setDescription(e.target.value)}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500" />
          </div>

          {/* Availability Setup - only on create */}
          {!isEdit && (
            <AvailabilitySetup value={availability} onChange={setAvailability} />
          )}

          {isEdit && initialData && (
            <div className="md:col-span-2">
              <ImageUpload
                entityType="venues"
                entityId={initialData.id}
                images={images}
                onImageAdded={handleImageAdded}
                onImageDeleted={handleImageDeleted}
                onPrimarySet={handlePrimarySet}
                addImageToApi={(data) => venueApi.addImage(initialData.id, data)}
                deleteImageFromApi={(imageId) => venueApi.deleteImage(initialData.id, imageId).then(() => {})}
                setPrimaryImageApi={(imageId) => venueApi.setPrimaryImage(initialData.id, imageId).then(() => {})}
              />
            </div>
          )}

          {isEdit && (
            <div className="flex items-center gap-2 md:col-span-2">
              <input type="checkbox" id="isActive" checked={isActive} onChange={(e) => setIsActive(e.target.checked)}
                className="h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500" />
              <label htmlFor="isActive" className="text-sm font-medium text-gray-700">Active</label>
            </div>
          )}
        </div>

        <div className="mt-6 flex justify-end">
          <button type="submit" disabled={loading}
            className="rounded-md bg-blue-600 px-6 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50">
            {loading ? 'Saving...' : isEdit ? 'Update Venue' : availability.enabled ? 'Create & Set Availability' : 'Create Venue'}
          </button>
        </div>
      </form>
    </div>
  )
}
