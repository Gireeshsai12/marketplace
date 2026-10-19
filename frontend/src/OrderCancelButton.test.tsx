import React from 'react';
import { renderToStaticMarkup } from 'react-dom/server';
import { describe, expect, it } from 'vitest';
import { OrderCancelButton } from './OrderCancelButton';

describe('order cancellation visibility', () => {
  it('omits cancellation when the server disallows it, including shipped orders', () => {
    expect(renderToStaticMarkup(<OrderCancelButton canCancel={false} busy={false} onCancel={() => {}} />)).toBe('');
  });
  it('allows cancellation of an eligible processing order', () => {
    expect(renderToStaticMarkup(<OrderCancelButton canCancel busy={false} onCancel={() => {}} />)).toContain('Cancel order');
  });
  it('disables cancellation while an action is pending', () => {
    expect(renderToStaticMarkup(<OrderCancelButton canCancel busy onCancel={() => {}} />)).toContain('disabled=""');
  });
});
