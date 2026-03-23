import { useState, useRef, type ChangeEvent, type DragEvent } from 'react'
import { Upload, Star, Trash2 } from 'lucide-react'
import { useImageUpload } from '@/hooks/useImageUpload'

interface BaseImageItem {
  id: number
  imageUrl: string
  isPrimary: boolean
  displayOrder: number
}

interface ImageUploadProps<T extends BaseImageItem> {
  entityType: 'venues' | 'coaches'
  entityId: number
  images: T[]
  onImageAdded: (image: T) => void
  onImageDeleted: (imageId: number) => void
  onPrimarySet: (imageId: number) => void
  addImageToApi: (data: { imageUrl: string; isPrimary: boolean; displayOrder: number }) => Promise<{ data: T }>
  deleteImageFromApi: (imageId: number) => Promise<unknown>
  setPrimaryImageApi: (imageId: number) => Promise<unknown>
}

export function ImageUpload<T extends BaseImageItem>({
  entityType,
  entityId,
  images,
  onImageAdded,
  onImageDeleted,
  onPrimarySet,
  addImageToApi,
  deleteImageFromApi,
  setPrimaryImageApi,
}: ImageUploadProps<T>) {
  const { uploading, progress, error: uploadError, uploadImage } = useImageUpload()
  const [dragOver, setDragOver] = useState(false)
  const [actionError, setActionError] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const handleFiles = async (files: FileList | null) => {
    if (!files || files.length === 0) return
    setActionError(null)

    for (const file of Array.from(files)) {
      try {
        const timestamp = Date.now()
        const safeName = file.name.replace(/[^a-zA-Z0-9._-]/g, '_')
        const storagePath = `${entityType}/${entityId}/${timestamp}_${safeName}`

        const downloadUrl = await uploadImage(file, storagePath)

        const isPrimary = images.length === 0
        const displayOrder = images.length

        const response = await addImageToApi({
          imageUrl: downloadUrl,
          isPrimary,
          displayOrder,
        })
        onImageAdded(response.data)
      } catch (err) {
        const msg = err instanceof Error ? err.message : 'Failed to upload image'
        setActionError(msg)
      }
    }
  }

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    handleFiles(e.target.files)
    if (fileInputRef.current) fileInputRef.current.value = ''
  }

  const handleDrop = (e: DragEvent) => {
    e.preventDefault()
    setDragOver(false)
    handleFiles(e.dataTransfer.files)
  }

  const handleDragOver = (e: DragEvent) => {
    e.preventDefault()
    setDragOver(true)
  }

  const handleDragLeave = () => setDragOver(false)

  const handleDelete = async (imageId: number) => {
    setActionError(null)
    try {
      await deleteImageFromApi(imageId)
      onImageDeleted(imageId)
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to delete image'
      setActionError(msg)
    }
  }

  const handleSetPrimary = async (imageId: number) => {
    setActionError(null)
    try {
      await setPrimaryImageApi(imageId)
      onPrimarySet(imageId)
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Failed to set primary image'
      setActionError(msg)
    }
  }

  const displayError = uploadError || actionError

  return (
    <div className="space-y-4">
      <label className="block text-sm font-medium text-gray-700">Images</label>

      {displayError && (
        <div className="rounded-md bg-red-50 p-3 text-sm text-red-700">{displayError}</div>
      )}

      {images.length > 0 && (
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-4">
          {images.map((img) => (
            <div key={img.id} className="group relative overflow-hidden rounded-lg border border-gray-200">
              <img
                src={img.imageUrl}
                alt=""
                className="h-32 w-full object-cover"
              />
              {img.isPrimary && (
                <span className="absolute left-1 top-1 rounded bg-yellow-400 px-1.5 py-0.5 text-xs font-semibold text-yellow-900">
                  Primary
                </span>
              )}
              <div className="absolute right-1 top-1 flex gap-1 opacity-0 transition-opacity group-hover:opacity-100">
                {!img.isPrimary && (
                  <button
                    type="button"
                    onClick={() => handleSetPrimary(img.id)}
                    className="rounded bg-white/90 p-1 text-gray-600 hover:text-yellow-600"
                    title="Set as primary"
                  >
                    <Star className="h-4 w-4" />
                  </button>
                )}
                <button
                  type="button"
                  onClick={() => handleDelete(img.id)}
                  className="rounded bg-white/90 p-1 text-gray-600 hover:text-red-600"
                  title="Delete image"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <div
        onDrop={handleDrop}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onClick={() => fileInputRef.current?.click()}
        className={`flex cursor-pointer flex-col items-center justify-center rounded-lg border-2 border-dashed p-6 transition-colors ${
          dragOver
            ? 'border-blue-400 bg-blue-50'
            : 'border-gray-300 hover:border-gray-400'
        }`}
      >
        <Upload className="mb-2 h-8 w-8 text-gray-400" />
        <p className="text-sm text-gray-600">
          {uploading
            ? `Uploading... ${progress}%`
            : 'Click or drag images here to upload'}
        </p>
        <p className="mt-1 text-xs text-gray-400">JPEG, PNG, or WebP. Max 5 MB each.</p>
        <input
          ref={fileInputRef}
          type="file"
          accept="image/jpeg,image/png,image/webp"
          multiple
          className="hidden"
          onChange={handleFileChange}
        />
      </div>

      {uploading && (
        <div className="h-2 overflow-hidden rounded-full bg-gray-200">
          <div
            className="h-full rounded-full bg-blue-600 transition-all"
            style={{ width: `${progress}%` }}
          />
        </div>
      )}
    </div>
  )
}
