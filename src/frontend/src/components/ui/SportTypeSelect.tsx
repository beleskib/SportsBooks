import { SportType, SPORT_TYPE_LABELS } from '@/types'

interface SportTypeSelectProps {
  value: string
  onChange: (value: string) => void
  disabled?: boolean
  required?: boolean
}

export function SportTypeSelect({ value, onChange, disabled, required }: SportTypeSelectProps) {
  return (
    <select
      value={value}
      onChange={(e) => onChange(e.target.value)}
      disabled={disabled}
      required={required}
      className="block w-full rounded-md border border-gray-300 px-3 py-2 shadow-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500 disabled:bg-gray-100 disabled:text-gray-500"
    >
      <option value="">Select sport type</option>
      {Object.values(SportType).map((type) => (
        <option key={type} value={type}>
          {SPORT_TYPE_LABELS[type]}
        </option>
      ))}
    </select>
  )
}
