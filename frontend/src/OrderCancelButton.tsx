import React from 'react';
type Props = { canCancel: boolean; busy: boolean; onCancel: () => void };

export function OrderCancelButton({ canCancel, busy, onCancel }: Props) {
  if (!canCancel) return null;
  return (
    <button className="secondary" disabled={busy} onClick={onCancel}>
      Cancel order
    </button>
  );
}
