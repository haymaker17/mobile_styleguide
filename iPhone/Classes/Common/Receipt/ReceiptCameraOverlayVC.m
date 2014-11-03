//
//  ReceiptCameraOverlayVC.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 3/8/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ReceiptCameraOverlayVC.h"
#import "ReceiptStoreDetailViewController.h"
#import "CCImageScrollViewController.h"
#import "ImageUtil.h"
#import "Config.h"

@interface ReceiptCameraOverlayVC ()

- (void)setupToolbar;
-(UIImage*)restrictImageSize:(UIImage*)originalImage;

@property (nonatomic,strong) UIImage *selectedImage;
@end

@implementation ReceiptCameraOverlayVC
@synthesize delegate;

#pragma mark -
#pragma mark ReceiptCameraOverlayVC
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    if ((self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil]))
    {
        self.imagePickerController = [[UIImagePickerController alloc] init];
        self.imagePickerController.delegate = self;
        self.imagePickerController.allowsEditing = NO;
        [self.imagePickerController setWantsFullScreenLayout:YES];

        inPreviewMode = NO;
    }
    
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib
    [self setupToolbar];
    
    // Initialize button images
    [self.btnDone setBackgroundImage:[[UIImage imageNamed:@"blue_button"]
                                      stretchableImageWithLeftCapWidth:12.0f
                                      topCapHeight:0.0f]
                            forState:UIControlStateNormal];
    [self.btnRetake setBackgroundImage:[[UIImage imageNamed:@"button_gray"]
                                        stretchableImageWithLeftCapWidth:12.0f
                                        topCapHeight:0.0f]
                              forState:UIControlStateNormal];
    
    // Disable flash toggle button if not available
    if(![UIImagePickerController isFlashAvailableForCameraDevice:UIImagePickerControllerCameraDeviceRear])
    {
         self.btnFlash.enabled = NO;
    }
    
    if ([ExSystem is5])
    {
        float bottomBarHeight = 96.0f;
        float imgHeight = self.view.frame.size.height - bottomBarHeight;
        CGRect fr = self.vwBottomBar.frame;
        self.vwBottomBar.frame = CGRectMake(fr.origin.x, imgHeight,  fr.size.width, bottomBarHeight);
    }
    if ([UIDevice isPad] &&  UIDeviceOrientationIsLandscape([[UIApplication sharedApplication] statusBarOrientation])) {
        [self.bottomBarImageView setAlpha:0.2];
    }
}

- (void)viewWillDisappear:(BOOL)animated
{
    //MOB-13029 Top status bar hidden after show UIImagePickerController
    if (self.imagePickerController.sourceType == UIImagePickerControllerSourceTypePhotoLibrary)
    {
        [[UIApplication sharedApplication] setStatusBarHidden:NO];
    }
}

- (void)viewDidUnload
{
    self.btnCancel = nil;
    self.btnDone = nil;
    self.btnFlash = nil;
    self.btnRetake = nil;
    self.BtnTakePicture = nil;
    self.vwBottomBar = nil;
    [super viewDidUnload];
}

- (void)setupToolbar
{
    if (inPreviewMode)
    {
        self.lblTitle.text = [@"Preview" localize];

        self.btnTakePicture.hidden = YES;
        self.btnRetake.hidden = NO;
        self.btnDone.hidden = NO;
    }
    else
    {
        self.lblTitle.text = self.title == nil? [@"Attach Receipt" localize] : self.title;

        if ([self.delegate shouldDisplayAlbum])
            self.btnAlbum.hidden = NO;
        
        BOOL deviceHasCamera = [UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera];
        self.btnTakePicture.hidden = !deviceHasCamera;
        
        self.btnRetake.hidden = YES;
        self.btnDone.hidden = YES;
    }
}

- (void)setupImagePicker:(UIImagePickerControllerSourceType)sourceType
{
    self.imagePickerController.sourceType = sourceType;
    if (sourceType == UIImagePickerControllerSourceTypeCamera)
    {
        // user wants to use the camera interface
        //
        self.imagePickerController.showsCameraControls = NO;
        if ([[self.imagePickerController.cameraOverlayView subviews] count] == 0)
        {
            // setup our custom overlay view for the camera
            //
            // ensure that our custom view's frame fits within the parent frame
            CGRect overlayViewFrame = self.imagePickerController.cameraOverlayView.frame;
            //if (![UIDevice isPad])
                self.view.frame = overlayViewFrame;
            // overlayViewFrame is the entire screen on iPad.
            [self.imagePickerController.cameraOverlayView addSubview:self.view];
        }
    }
}



// called when the parent application receives a memory warning
- (void)didReceiveMemoryWarning
{
    // we have been warned that memory is getting low, stop all timers
    //
    [super didReceiveMemoryWarning];
}



// update the UI after an image has been chosen or picture taken
//
- (void)finishAndUpdate
{
    [self.delegate didFinishWithCamera];  // tell our delegate we are done with the camera
    
    // TODO restore the state of our overlay toolbar buttons
//    
//    self.cancelButton.enabled = YES;
//    
//    self.takePictureButton.enabled = YES;
    
}


#pragma mark -
#pragma mark Camera Actions

- (IBAction)actionCancel:(id)sender
{
    // dismiss the camera
    [self finishAndUpdate];
}

- (IBAction)actionTakePhoto:(id)sender
{
    [self.imagePickerController takePicture];
}

- (IBAction)actionToggleFlash:(id)sender
{
    if (self.imagePickerController.cameraFlashMode == UIImagePickerControllerCameraFlashModeOn)
    {
        [self.imagePickerController setCameraFlashMode:UIImagePickerControllerCameraFlashModeOff];
        
        [self.btnFlash setTitle:[@"Off" localize] forState:UIControlStateNormal];
    }
    else
    {
        [self.imagePickerController setCameraFlashMode:UIImagePickerControllerCameraFlashModeOn];
        [self.btnFlash setTitle:[@"On" localize] forState:UIControlStateNormal];
    }
}

- (IBAction)actionReTakePhoto:(id)sender
{
    inPreviewMode = NO;
    [self setupToolbar];
}

- (IBAction)actionAlbum:(id)sender
{
    if([UIImagePickerController isSourceTypeAvailable: UIImagePickerControllerSourceTypePhotoLibrary])
    {
        NSDictionary *dict = @{@"Added Using": @"Album"};
        [Flurry logEvent:@"Receipts: Add" withParameters:dict];
        
        [self setupImagePicker:UIImagePickerControllerSourceTypePhotoLibrary];
    }
}

#pragma mark Selected Image Method
- (void) processSelectedImage:(UIImage*)image
{
    UIImage* smallerImage = [self restrictImageSize:image];
    
    // give the taken picture to our delegate
    [self.delegate didTakePicture:smallerImage];
}

#pragma mark -
#pragma mark UIImagePickerControllerDelegate
// this gets called when an image has been chosen from the library or taken from the camera
- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info
{
    UIImage* originalImage = [info valueForKey:UIImagePickerControllerOriginalImage];

    // TODO: create a really unified image picker
    CCImageScrollViewController *ctrl  = [[UIStoryboard storyboardWithName:@"CCImageScrollStoryboard" bundle:nil] instantiateInitialViewController];
    [ctrl setImage:originalImage];
    self.selectedImage = originalImage;
    [ctrl.navigationItem setTitle:[Localizer getLocalizedText:@"ChoosePhoto-ImagePicker"]];
        
        
    [ctrl.navigationItem setRightBarButtonItem:[[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Use-ImagePicker"]
                                                                                    style:UIBarButtonItemStyleBordered
                                                                                   target:self
                                                                                   action:@selector(useSelectedImage)]];
    //MOB-13029 Top status bar hidden after show UIImagePickerController
    [[UIApplication sharedApplication] setStatusBarHidden:NO];
    [picker pushViewController:ctrl animated:YES];
    ctrl.navigationController.navigationBarHidden = NO;
}

- (void)useSelectedImage{
    [self processSelectedImage:self.selectedImage];
}

- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker
{
    [self.delegate didFinishWithCamera];    // tell our delegate we are finished with the picker
}

- (void)pictureConfirmed:(UIImage *)image
{    
    [self processSelectedImage:image];
}


#pragma mark -
#pragma mark Image Scaling
// Copied from UnifiedImagePicker.delayedImageManipulationForSize
-(UIImage*)restrictImageSize:(UIImage*)originalImage
{
    UIImage* result = originalImage;
    
    int baseScaler = 1000;
    float scaler = 1;
    float w = originalImage.size.width;
	float h = originalImage.size.height;
    
    if(w <= 0)
		w = 1;
    
    if (h >= w) {
        scaler = h/baseScaler;
    }
    else {
        scaler = w/baseScaler;
    }
	
    if (w >= baseScaler || h >= baseScaler) {
        
        h = h/scaler;
        w = w/scaler;
        
        CGSize newSize = CGSizeMake(w , h);
        UIImage * img = nil;
        img = [ImageUtil imageWithImage:originalImage scaledToSize:newSize];
        result = img;
#ifdef TEST_LOG
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"Image size before: %@", NSStringFromCGSize(originalImage.size)] Level:MC_LOG_DEBU];
        [[MCLogging getInstance] log:[NSString stringWithFormat:@"Image size after: %@", NSStringFromCGSize(result.size)] Level:MC_LOG_DEBU];
#endif
        
    }
    
    return result;
}

@end
