//
//  UnifiedImagePicker.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 1/14/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "UnifiedImagePicker.h"
#import "ImageUtil.h"
#import "CCImageScrollViewController.h"
#import "AssetsLibrary/ALAssetsLibrary.h"

static UnifiedImagePicker* sharedInstance;

@interface UnifiedImagePicker ()
-(void)delayedImageManipulationForSize:(NSNumber*)selectedSizeOption;
-(void)sendImageToDelegate;
@end

@implementation UnifiedImagePicker


+(UnifiedImagePicker*)sharedInstance
{
	if (sharedInstance != nil) 
	{
		return sharedInstance;
	}
	else 
	{
		@synchronized (self)
		{
			if (sharedInstance == nil) 
			{
				sharedInstance = [[UnifiedImagePicker alloc] init];
			}
		}
		return sharedInstance;
	}
}

-(UnifiedImagePicker*)init
{
	if ((self = [super init])) 
	{
		self.imagePicker = [[UIImagePickerController alloc] init];
		// Mob-4527: Image preview 
		[self.imagePicker setAllowsEditing:YES];
        [self.imagePicker setWantsFullScreenLayout:YES];
        [self.imagePicker setHidesBottomBarWhenPushed:YES];
		self.imagePicker.delegate = self;
		self.delegate = nil;
        self.originalImage = nil;
	}
	return self;
}



#pragma mark -
#pragma mark Image Scaling
-(void)delayedImageManipulationForSize:(NSNumber*)selectedSizeOption
{
    int sizeOption = [selectedSizeOption intValue];
    int baseScaler = 1;
    
    switch (sizeOption) {
        case 0:
            baseScaler = 320;
            break;
        case 1:
            baseScaler = 640;
            break;
        case 2:
            baseScaler = 1000;
            break;
        default:
            break;
    }
    
    float scaler = 1;
    float w = self.originalImage.size.width;
	float h = self.originalImage.size.height;
    
#ifdef TEST_LOG    
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"Image size before: %@", NSStringFromCGSize(originalImage.size)] Level:MC_LOG_DEBU];
#endif
    
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
        img = [ImageUtil imageWithImage:self.originalImage scaledToSize:newSize];
        self.originalImage = img;
    }
    
#ifdef TEST_LOG  
    [[MCLogging getInstance] log:[NSString stringWithFormat:@"Image size after: %@", NSStringFromCGSize(originalImage.size)] Level:MC_LOG_DEBU];
#endif    
}

-(void)sendImageToDelegate
{
    // fix for MOB-14335 
    [self.imagePicker popToRootViewControllerAnimated:NO];
	if ([self.delegate respondsToSelector:@selector(unifiedImagePickerSelectedImage:)])
	{
		[self.delegate unifiedImagePickerSelectedImage:self.originalImage];
	}
}

#pragma mark Copy methods
//- (void) image:(UIImage *)image didFinishSavingWithError:(NSError *)error contextInfo:(void *)contextInfo
//{	
//	if (error != nil)
//	{
//		NSLog(@"%@", [error localizedDescription]);
//	}
//    else
//    {
//        self.originalImage = image;
//        [self performSelector:@selector(delayedImageManipulationForSize:) withObject:[NSNumber numberWithInt:2] afterDelay:0.0005];
//    }
//}


-(void) copyToPhotoAlbum:(UIImage*)img
{
    self.originalImage = img;
    
    if (img != nil) {
        ALAssetsLibrary *library = [[ALAssetsLibrary alloc] init];
        @try {
            [library writeImageToSavedPhotosAlbum:[img CGImage] orientation:(ALAssetOrientation)[img imageOrientation] completionBlock:^(NSURL *imageURL, NSError *error){
                if (error) {  
                    NSLog(@"error");  
                } else {  
                    NSLog(@"url %@", imageURL);  
                }  
            }];
        }
        @catch (NSException *exception) {
            NSLog(@"%@",exception);
        }
        
    }
}


#pragma mark -
#pragma mark UIImagePickerController delegate methods
- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary *)info
{
    // ignore Move & Scale as it causes other issues with scaling
	self.originalImage = info[UIImagePickerControllerOriginalImage] ; 
    [self delayedImageManipulationForSize:@2];
    
    // TODO: create a really unified image picker
    
    // show pinch and zoom preview if selecting from camera roll
    CCImageScrollViewController *ctrl  = [[UIStoryboard storyboardWithName:@"CCImageScrollStoryboard" bundle:nil] instantiateInitialViewController];
    [ctrl setImage:self.originalImage];
    
    [ctrl.navigationItem setTitle:[Localizer getLocalizedText:@"ChoosePhoto-ImagePicker"]];
         
    [ctrl.navigationItem setRightBarButtonItem:[[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Use-ImagePicker"]
                                                                                    style:UIBarButtonItemStyleBordered
                                                                                   target:self
                                                                                   action:@selector(sendImageToDelegate)]];
    [picker pushViewController:ctrl animated:YES];
    ctrl.navigationController.navigationBarHidden = NO;
    [[UIApplication sharedApplication] setStatusBarHidden:NO];
}

-(void)imagePickerControllerDidCancel:(UIImagePickerController *)imgPicker
{
	[imgPicker dismissViewControllerAnimated:YES completion:nil];
    if ([self.delegate respondsToSelector:@selector(imagePickerControllerDidCancelAction)]) {
        [self.delegate performSelector:@selector(imagePickerControllerDidCancelAction)];
    }
}

@end
