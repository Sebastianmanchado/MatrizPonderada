import { forwardRef } from 'react';
import { cn } from '../../lib/cn';

export const Input = forwardRef(function Input({ className, ...props }, ref) {
  return (
    <input
      ref={ref}
      className={cn(
        'block w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm shadow-sm',
        'placeholder:text-slate-400',
        'focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500',
        'disabled:bg-slate-100',
        className
      )}
      {...props}
    />
  );
});

export const Textarea = forwardRef(function Textarea({ className, ...props }, ref) {
  return (
    <textarea
      ref={ref}
      className={cn(
        'block w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm shadow-sm',
        'placeholder:text-slate-400 min-h-[80px]',
        'focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500',
        className
      )}
      {...props}
    />
  );
});

export const Select = forwardRef(function Select({ className, children, ...props }, ref) {
  return (
    <select
      ref={ref}
      className={cn(
        'block w-full rounded-md border border-slate-300 bg-white px-3 py-2 text-sm shadow-sm',
        'focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500',
        className
      )}
      {...props}
    >
      {children}
    </select>
  );
});

export function Field({ label, error, hint, children, className }) {
  return (
    <div className={cn('space-y-1', className)}>
      {label && <label className="block text-sm font-medium text-slate-700">{label}</label>}
      {children}
      {hint && !error && <p className="text-xs text-slate-500">{hint}</p>}
      {error && <p className="text-xs text-red-600">{error}</p>}
    </div>
  );
}
