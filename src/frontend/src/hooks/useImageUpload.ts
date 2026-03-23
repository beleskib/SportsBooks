import { useState, useCallback } from 'react'
import { ref, uploadBytesResumable, getDownloadURL } from 'firebase/storage'
import { storage } from '@/config/firebase'

interface UseImageUploadReturn {
  uploading: boolean
  progress: number
  error: string | null
  uploadImage: (file: File, path: string) => Promise<string>
}

const MAX_FILE_SIZE = 5 * 1024 * 1024 // 5 MB
const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']

export function useImageUpload(): UseImageUploadReturn {
  const [uploading, setUploading] = useState(false)
  const [progress, setProgress] = useState(0)
  const [error, setError] = useState<string | null>(null)

  const uploadImage = useCallback(async (file: File, path: string): Promise<string> => {
    if (!ALLOWED_TYPES.includes(file.type)) {
      throw new Error('Only JPEG, PNG, and WebP images are allowed')
    }
    if (file.size > MAX_FILE_SIZE) {
      throw new Error('Image must be less than 5 MB')
    }

    setUploading(true)
    setProgress(0)
    setError(null)

    try {
      const storageRef = ref(storage, path)
      const uploadTask = uploadBytesResumable(storageRef, file, {
        contentType: file.type,
      })

      return await new Promise<string>((resolve, reject) => {
        uploadTask.on(
          'state_changed',
          (snapshot) => {
            const pct = (snapshot.bytesTransferred / snapshot.totalBytes) * 100
            setProgress(Math.round(pct))
          },
          (err) => {
            setError(err.message)
            setUploading(false)
            reject(err)
          },
          async () => {
            const downloadUrl = await getDownloadURL(uploadTask.snapshot.ref)
            setUploading(false)
            setProgress(100)
            resolve(downloadUrl)
          }
        )
      })
    } catch (err) {
      setUploading(false)
      const msg = err instanceof Error ? err.message : 'Upload failed'
      setError(msg)
      throw err
    }
  }, [])

  return { uploading, progress, error, uploadImage }
}
