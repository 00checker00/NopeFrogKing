package de.nopefrogking.ios

import apple.coregraphics.struct.CGPoint
import apple.coregraphics.struct.CGRect
import apple.coregraphics.struct.CGSize
import apple.foundation.NSArray
import apple.foundation.NSDictionary
import apple.uikit.UIApplication
import apple.uikit.UIScreen
import apple.uikit.UIViewController
import apple.uikit.c.UIKit
import apple.uikit.protocol.UIApplicationDelegate
import com.badlogic.gdx.backends.iosmoe.IOSApplication
import com.badlogic.gdx.backends.iosmoe.IOSApplicationConfiguration
import com.google.firebasecore.FIRApp
import de.nopefrogking.ADMOB_APP_ID
import de.nopefrogking.ADMOB_BANNER_ID
import de.nopefrogking.ADMOB_INTERSTITIAL_ID
import de.nopefrogking.Main
import de.nopefrogking.utils.PlatformHandler
import de.nopefrogking.utils.RegisterPlatformHandler
import com.google.googlemobileads.GADBannerView
import com.google.googlemobileads.GADInterstitial
import com.google.googlemobileads.GADMobileAds
import com.google.googlemobileads.GADRequest
import com.google.googlemobileads.c.GoogleMobileAds
import org.moe.natj.general.Pointer
import org.moe.natj.objc.ann.ObjCClassName
import org.moe.natj.objc.ann.Selector

@ObjCClassName("IOSMoeLauncher")
class IOSMoeLauncher protected constructor(peer: Pointer) : IOSApplication.Delegate(peer), UIApplicationDelegate {
    //
    private lateinit var gdxApp: IOSApplication
    private lateinit var uiViewController: UIViewController
    private lateinit var adView: GADBannerView

    override fun createApplication(): IOSApplication {
        val config = IOSApplicationConfiguration()
        config.useAccelerometer = false
        gdxApp = IOSApplication(Main(), config)
        return gdxApp
    }

    override fun applicationDidFinishLaunchingWithOptions(application: UIApplication?, launchOptions: NSDictionary<*, *>?): Boolean {
        FIRApp.initialize()
        GADMobileAds.configureWithApplicationID(ADMOB_APP_ID)
        return super<IOSApplication.Delegate>.applicationDidFinishLaunchingWithOptions(application, launchOptions)
    }

    override fun applicationDidBecomeActive(application: UIApplication?) {

        uiViewController = gdxApp.uiViewController

        adView = GADBannerView.alloc().initWithAdSize(GoogleMobileAds.kGADAdSizeBanner())
        adView.setAdUnitID(ADMOB_BANNER_ID)


        val cgRect = CGRect()
        cgRect.setSize(CGSize(GoogleMobileAds.kGADAdSizeBanner().size().width() * 0.7, GoogleMobileAds.kGADAdSizeBanner().size().height()))

        adView.setFrame(cgRect)
        adView.setRootViewController(uiViewController)

        val request = GADRequest.request()

        request.setTestDevices(NSArray.arrayWithObject(GoogleMobileAds.kGADSimulatorID()))

        adView.loadRequest(request)

        adView.setCenter(CGPoint(UIScreen.mainScreen().bounds().size().width() * 0.5f, UIScreen.mainScreen().bounds().size().height() - GoogleMobileAds.kGADAdSizeBanner().size().height()/2))

        gdxApp.uiWindow.addSubview(adView)

        val gadInterstitial = GADInterstitial.alloc().initWithAdUnitID(ADMOB_INTERSTITIAL_ID)
        gadInterstitial.loadRequest(request)

        RegisterPlatformHandler(IOSPlatformHandler(adView, gadInterstitial, uiViewController, gdxApp!!))

        super<IOSApplication.Delegate>.applicationDidBecomeActive(application)
    }

    companion object {

        @Selector("alloc")
        @JvmStatic external fun alloc(): IOSMoeLauncher

        @JvmStatic fun main(argv: Array<String>) {
            UIKit.UIApplicationMain(0, null, null, IOSMoeLauncher::class.java.simpleName)
        }
    }
}
