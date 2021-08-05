using Android.App;
using Android.Content;
using Android.Hardware.Usb;
using System;
using System.Threading;
using System.Threading.Tasks;
using Xamarin.Essentials;
using Context = Android.Content.Context;

namespace LibaumsSample
{
    public static class PermissionHelper
    {
        private const string ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

        private static ManualResetEventSlim _mres;
        private static TaskCompletionSource<PermissionStatus> _usbPermissionTcs;

        public static Task<PermissionStatus> RequestUsbPermissionAsync(Context ctx, UsbDevice usbDevice, TimeSpan? timeout = null)
        {
            timeout ??= TimeSpan.FromSeconds(10);
            var usbManager = (UsbManager)ctx.GetSystemService(Context.UsbService);
            if (usbManager.HasPermission(usbDevice))
                return Task.FromResult(PermissionStatus.Granted);

            var receiver = new UsbPermissionBroadcastReceiver();
            var filter = new IntentFilter(ACTION_USB_PERMISSION);

            ctx.RegisterReceiver(receiver, filter);

            _usbPermissionTcs = new TaskCompletionSource<PermissionStatus>();

            var permissionIntent = PendingIntent.GetBroadcast(ctx, 0, new Intent(ACTION_USB_PERMISSION), 0);
            usbManager.RequestPermission(usbDevice, permissionIntent);

            Task.Factory.StartNew(() =>
            {
                _mres = new ManualResetEventSlim(false);

                if (!_mres.Wait(timeout.Value))
                {
                    _usbPermissionTcs.SetResult(PermissionStatus.Unknown);
                }

                _usbPermissionTcs = null;
                _mres = null;
            });

            _usbPermissionTcs.Task.ContinueWith(t => { ctx.UnregisterReceiver(receiver); });
            return _usbPermissionTcs.Task;
        }

        class UsbPermissionBroadcastReceiver : BroadcastReceiver
        {
            public override void OnReceive(Context? context, Intent? intent)
            {
                string action = intent.Action;
                if (ACTION_USB_PERMISSION != action)
                {
                    return;
                }

                //UsbAccessory accessory = (UsbAccessory)intent.GetParcelableExtra(UsbManager.ExtraAccessory);

                if (intent.GetBooleanExtra(UsbManager.ExtraPermissionGranted, false))
                {
                    _usbPermissionTcs.SetResult(PermissionStatus.Granted);
                }
                else
                {
                    _usbPermissionTcs.SetResult(PermissionStatus.Denied);
                }

                _mres.Set();
            }
        }
    }
}