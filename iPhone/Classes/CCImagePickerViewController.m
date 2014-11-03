//
//  CCImagePickerViewController.m
//  TestImagePicker
//
//  Created by Sally Yan on 8/8/13.
//  Copyright (c) 2013 Sally Yan. All rights reserved.
//

#import "CCImagePickerViewController.h"
#import "CCImageScrollViewController.h"
#import "ImageUtil.h"
#import "ExSystem.h"

@interface CCImagePickerViewController ()
{
    ReceiptCameraOverlayView *cameraOverlayView;
}
@end

@interface CCImagePickerViewController ()

@property (strong, nonatomic) UIImage *imageTaken;

@end

@implementation CCImagePickerViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
}

- (void)viewDidAppear:(BOOL)animated{
    [super viewDidAppear:animated];
    
    // MOB-20919: do not present camera or album view if user pressed cancel button
    if (self.isToShowImagePickerView) {
        if (self.albumSelected || ![UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera])
            [self albumPressed:nil];
        else
            [self cameraPressed:nil];
    }
 }

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (CGSize)contentSizeForViewInPopover {
    return (CGSize){320,480};
}

- (void)cameraPressed:(id)sender {
    self.picker = [[UIImagePickerController alloc] init];
    
    self.picker.sourceType = UIImagePickerControllerSourceTypeCamera ;
    self.picker.cameraFlashMode = UIImagePickerControllerCameraFlashModeOff;
    self.picker.delegate = self;
    
    cameraOverlayView = [[[NSBundle mainBundle] loadNibNamed:@"ReceiptCameraOverlay" owner:self options:nil] objectAtIndex:0];
	//MOB-16301 -- localize
    [cameraOverlayView setUIText];

    if ([UIDevice isPad])   // if we are in a popover
    {
        [cameraOverlayView.albumTapped setHidden:YES];
    }
    [cameraOverlayView.cancelTapped addTarget:self action:@selector(cancelTapped:) forControlEvents:UIControlEventTouchUpInside];
    
    [cameraOverlayView.flashOnOffTapped addTarget:self action:@selector(flashOnOffTapped:) forControlEvents:UIControlEventTouchUpInside];
    
    [cameraOverlayView.albumTapped addTarget:self action:@selector(albumTapped:) forControlEvents:UIControlEventTouchUpInside];
    
    [cameraOverlayView.takePictureTapped addTarget:self action:@selector(takePictureTapped:) forControlEvents:UIControlEventTouchUpInside];
    
    // TODO: Make this dynamic...
    //
    // 44 = height of top bar.
    // 392 = 480 - (44 * 2)
    //
    CGAffineTransform translate = CGAffineTransformMakeTranslation(0.0, 44.0);
    
    self.picker.cameraViewTransform = CGAffineTransformScale(translate, 480.0/392.0, 480.0/392.0);
    
    [self.picker setShowsCameraControls:NO];
    [self.picker setCameraOverlayView:cameraOverlayView];
    [self presentViewController:self.picker animated:YES completion:nil];
}

- (void)albumPressed:(id)sender {
    self.picker = [[UIImagePickerController alloc] init];
    self.picker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary ;
    self.picker.delegate = self;

    if (self.presentingViewController == nil)   // if we are in a popover
    {
        [self addChildViewController:self.picker];
        [self.view addSubview:self.picker.view];
        CGSize sizeInPopover = [self contentSizeForViewInPopover];
        self.picker.view.frame = CGRectMake(0,0,sizeInPopover.width,sizeInPopover.height);
   }
    else
    {
        [self presentViewController:self.picker animated:YES completion:nil];
    }
}

- (void)imagePickerController:(UIImagePickerController *)pickerController didFinishPickingMediaWithInfo:(NSDictionary *)info {

    CCImageScrollViewController *preview = [[UIStoryboard storyboardWithName:@"CCImageScrollStoryboard" bundle:nil] instantiateInitialViewController];
    
    [preview setTitle:[Localizer getLocalizedText:@"Preview"]];
    self.imageTaken = [info valueForKey:UIImagePickerControllerOriginalImage];
    [preview setImage:self.imageTaken];

    [preview.titleBar setBackgroundColor:[UIColor clearColor]];
    [preview.titleItem setTitleTextAttributes:[NSDictionary dictionaryWithObjectsAndKeys: [UIColor whiteColor], UITextAttributeTextColor,nil] forState:UIControlStateNormal];
 
    UIBarButtonItem *retake;
    UIBarButtonItem *expense;
    UIBarButtonItem *done ;
    UIBarButtonItem *flexSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    
    if (self.retake) {
        NSString *selString = @"retakeTapped:";
        retake = [ExSystem makeSilverBarButton:[@"Retake" localize] width:70.0 height:32.0 selectorString:selString target:self];
    }
    
    if (self.expense) {
        NSString *selString = @"expenseTapped:";
        expense = [ExSystem makeColoredButton:@"BLUE" W:105 H:32 Text:[@"Expense" localize] SelectorString:selString MobileVC:(MobileViewController*)self];
    }
    
    if (self.done) {
        NSString *selString = @"doneTapped:";
        done = [ExSystem makeSilverBarButton:[@"Done" localize] width:70.0 height:32.0 selectorString:selString target:self];
    }

    preview.actionItems = [NSArray arrayWithNotNilObjects:5,retake,flexSpace,expense,flexSpace,done];
    
    if (pickerController.sourceType == UIImagePickerControllerSourceTypeCamera) {
        
        [pickerController presentViewController:preview animated:NO completion:^{
        	//MOB-16948 :"Preview" title text to white color.
            [preview.titleItem setTitleTextAttributes:[NSDictionary dictionaryWithObjectsAndKeys: [UIColor whiteColor], UITextAttributeTextColor,nil] forState:UIControlStateNormal];
        }];
    }
    
    if (pickerController.sourceType == UIImagePickerControllerSourceTypePhotoLibrary) {
        if (self.presentingViewController == nil)   // if we are in a popover
        {
            [pickerController presentViewController:preview animated:NO completion:^{}];
        }
        else
        {
            [pickerController pushViewController:preview animated:YES];
        }
    }
}


- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker{
    [[UIApplication sharedApplication] setStatusBarHidden:NO];
    
    if (self.cancel) {
        [self dismissViewControllerAnimated:NO completion:nil];
        self.cancel(self);
    }
}


#pragma mark - Events/ Gesture Reconizers

- (void)cancelTapped:(id)sender{
    _isToShowImagePickerView = NO;
    if (self.cancel) {
        self.cancel(self);
    }
}

- (void)flashOnOffTapped:(id)sender{
    if (self.picker.cameraFlashMode == UIImagePickerControllerCameraFlashModeOn) {
        self.picker.cameraFlashMode = UIImagePickerControllerCameraFlashModeOff;
        UIButton *flashOnOff = sender;
        [flashOnOff setTitle:[@"No" localize]  forState:UIControlStateNormal];
    }
    else
    {
        self.picker.cameraFlashMode = UIImagePickerControllerCameraFlashModeOn;
        UIButton *flashOnOff = sender;
        [flashOnOff setTitle:[@"Yes" localize] forState:UIControlStateNormal];
    }
}

- (void)albumTapped:(id)sender {
    _isToShowImagePickerView = NO;
    self.picker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
    self.picker.allowsEditing = NO;
    self.picker.delegate = self;
 }

- (void)cameraTapped:(id)sender {
    self.picker.sourceType = UIImagePickerControllerSourceTypeCamera;
}

- (void)takePictureTapped:(id)sender {
    [self.picker takePicture];
}

- (void)retakeTapped:(id)sender {
    _isToShowImagePickerView = YES;
    if (self.retake) {
        [[UIApplication sharedApplication] setStatusBarHidden:NO];
        self.albumSelected = nil;
        self.retake(self,@{UIImagePickerControllerOriginalImage: self.imageTaken});
    }
}

- (void)expenseTapped:(id)sender {
    _isToShowImagePickerView = NO;
    if (self.expense) {
        [[UIApplication sharedApplication] setStatusBarHidden:NO];
        self.expense(self,@{UIImagePickerControllerOriginalImage: self.imageTaken});
    }
}

- (void)doneTapped:(id)sender {
    _isToShowImagePickerView = NO;
    if (self.done) {
        [[UIApplication sharedApplication] setStatusBarHidden:NO];
        self.done(self,@{UIImagePickerControllerOriginalImage: self.imageTaken});
    }
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
