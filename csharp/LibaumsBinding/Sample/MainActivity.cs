using Android.App;
using Android.OS;
using Android.Runtime;
using AndroidX.AppCompat.App;
using System;
using System.Text;
using ME.Jahnen.Libaums.Core;
using Xamarin.Essentials;

namespace LibaumsSample
{
    [Activity(Label = "@string/app_name", Theme = "@style/AppTheme", MainLauncher = true)]
    public class MainActivity : AppCompatActivity
    {
        protected override async void OnCreate(Bundle savedInstanceState)
        {
            base.OnCreate(savedInstanceState);
            Platform.Init(this, savedInstanceState);
            // Set our view from the "main" layout resource
            SetContentView(Resource.Layout.activity_main);

            var devices = UsbMassStorageDevice.GetMassStorageDevices(this);
            var sb = new StringBuilder();
            foreach (var device in devices)
            {
                var name = device.UsbDevice.DeviceName;
                if (Build.VERSION.SdkInt >= BuildVersionCodes.Lollipop)
                {
                    name = device.UsbDevice.ManufacturerName + " " + device.UsbDevice.ProductName;
                }

                var permission = await PermissionHelper.RequestUsbPermissionAsync(this, device.UsbDevice);
                if (permission != PermissionStatus.Granted)
                {
                    Console.WriteLine("No permission to access:" + name);
                    continue;
                }

                // before interacting with a device you need to call init()!
                device.Init();
                // Only uses the first partition on the device
                var fs = device.Partitions[0].FileSystem;
                sb.AppendLine($"Name: {name}");
                sb.AppendLine($"Capacity: {FormatFileSize(fs.Capacity)}");
                sb.AppendLine($"Occupied Space: {FormatFileSize(fs.OccupiedSpace)}");
                sb.AppendLine($"Free Space: {FormatFileSize(fs.FreeSpace)}");
                sb.AppendLine($"Chunk size: {FormatFileSize(fs.ChunkSize)}");
                sb.AppendLine("======= root files: ======");
                var root = fs.RootDirectory;
                foreach (var file in root.ListFiles())
                {
                    sb.Append("\t");
                    sb.Append(file.IsDirectory ? "folder" : "file").Append(":");
                    sb.AppendLine(file.Name);
                }

                sb.AppendLine();
            }

            Console.WriteLine(sb);
        }

        private string FormatFileSize(long bytes)
        {
            return Android.Text.Format.Formatter.FormatShortFileSize(this, bytes);
        }

        public override void OnRequestPermissionsResult(int requestCode, string[] permissions, [GeneratedEnum] Android.Content.PM.Permission[] grantResults)
        {
            Platform.OnRequestPermissionsResult(requestCode, permissions, grantResults);

            base.OnRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}