import React from 'react';
import { useApp } from '../context/AppContext';
import { Download, CheckCircle, Sparkles, X, RefreshCw } from 'lucide-react';

export const AppUpdateModal: React.FC = () => {
  const {
    updateModalOpen,
    appUpdateInfo,
    isDownloadingUpdate,
    downloadProgress,
    readyToInstall,
    downloadAndInstallUpdate,
    dismissUpdatePrompt,
    isBangla,
  } = useApp();

  if (!updateModalOpen || !appUpdateInfo) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 p-4 backdrop-blur-xs">
      <div className="relative w-full max-w-md rounded-2xl bg-white p-6 shadow-2xl">
        <button
          onClick={dismissUpdatePrompt}
          className="absolute right-4 top-4 rounded-lg p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-700"
        >
          <X className="h-5 w-5" />
        </button>

        <div className="flex items-center gap-3">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-emerald-100 text-emerald-700">
            <Sparkles className="h-6 w-6" />
          </div>
          <div>
            <h3 className="text-base font-bold text-slate-900">
              {isBangla ? 'নতুন সংস্করণ উপলব্ধ' : 'New Update Available'}
            </h3>
            <p className="text-xs text-slate-500">
              v{appUpdateInfo.latestVersionName} • {isBangla ? 'বর্তমান:' : 'Current:'} v1.0.0
            </p>
          </div>
        </div>

        {/* Release Notes */}
        <div className="mt-4 rounded-xl border border-slate-200 bg-slate-50 p-3">
          <p className="text-xs font-semibold text-slate-700 mb-1">
            {isBangla ? 'আপডেটের নতুন সুবিধাসমূহ:' : "What's New:"}
          </p>
          <pre className="whitespace-pre-wrap font-sans text-xs text-slate-600 leading-relaxed">
            {appUpdateInfo.releaseNotes}
          </pre>
        </div>

        {/* Downloading State */}
        {isDownloadingUpdate && (
          <div className="mt-4 space-y-2">
            <div className="flex items-center justify-between text-xs text-slate-600 font-medium">
              <span>{isBangla ? 'ডাউনলোড হচ্ছে...' : 'Downloading update...'}</span>
              <span>{downloadProgress}%</span>
            </div>
            <div className="h-2 w-full overflow-hidden rounded-full bg-slate-100">
              <div
                className="h-full bg-emerald-600 transition-all duration-300"
                style={{ width: `${downloadProgress}%` }}
              />
            </div>
          </div>
        )}

        {/* Ready to install state */}
        {readyToInstall && (
          <div className="mt-4 flex items-center gap-2 rounded-xl bg-emerald-50 p-3 text-xs text-emerald-800">
            <CheckCircle className="h-5 w-5 text-emerald-600 shrink-0" />
            <span>
              {isBangla
                ? 'আপডেট সফলভাবে ডাউনলোড সম্পন্ন হয়েছে! রিফ্রেশ করে চালু করুন।'
                : 'Update downloaded! Restart or reload to apply.'}
            </span>
          </div>
        )}

        {/* Footer Actions */}
        <div className="mt-6 flex items-center justify-end gap-2">
          <button
            onClick={dismissUpdatePrompt}
            className="rounded-xl border border-slate-200 px-4 py-2 text-xs font-semibold text-slate-600 hover:bg-slate-50"
          >
            {isBangla ? 'পরে' : 'Later'}
          </button>

          {!readyToInstall ? (
            <button
              onClick={() => downloadAndInstallUpdate(appUpdateInfo)}
              disabled={isDownloadingUpdate}
              className="flex items-center gap-1.5 rounded-xl bg-emerald-600 px-4 py-2 text-xs font-semibold text-white shadow-sm hover:bg-emerald-700 disabled:opacity-50"
            >
              {isDownloadingUpdate ? (
                <>
                  <RefreshCw className="h-3.5 w-3.5 animate-spin" />
                  <span>{isBangla ? 'ডাউনলোড হচ্ছে...' : 'Downloading...'}</span>
                </>
              ) : (
                <>
                  <Download className="h-3.5 w-3.5" />
                  <span>{isBangla ? 'এখনই আপডেট করুন' : 'Update Now'}</span>
                </>
              )}
            </button>
          ) : (
            <button
              onClick={() => window.location.reload()}
              className="flex items-center gap-1.5 rounded-xl bg-emerald-600 px-4 py-2 text-xs font-semibold text-white shadow-sm hover:bg-emerald-700"
            >
              <RefreshCw className="h-3.5 w-3.5" />
              <span>{isBangla ? 'অ্যাপ রিলোড করুন' : 'Reload App'}</span>
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
