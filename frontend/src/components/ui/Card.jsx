import { cn } from '../../lib/cn';

export function Card({ className, ...props }) {
  return (
    <div
      className={cn(
        'rounded-lg border border-slate-200 bg-white shadow-sm',
        className
      )}
      {...props}
    />
  );
}

export function CardHeader({ className, ...props }) {
  return <div className={cn('p-5 border-b border-slate-200', className)} {...props} />;
}

export function CardTitle({ className, ...props }) {
  return <h2 className={cn('text-lg font-semibold text-slate-900', className)} {...props} />;
}

export function CardBody({ className, ...props }) {
  return <div className={cn('p-5', className)} {...props} />;
}
