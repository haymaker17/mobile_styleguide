//
//  ReceiptCameraOverlayVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 3/8/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol ReceiptCameraOverlayVCDelegate
- (void)didTakePicture:(UIImage *)picture;
- (void)didFinishWithCamera;
- (BOOL)shouldDisplayAlbum;
@end

// This is the receipt overlay view used in receipt camera choice and home receipt quick capture
@interface ReceiptCameraOverlayVC : MobileViewController<
    UINavigationControllerDelegate,
    UIImagePickerControllerDelegate,
    UIAlertViewDelegate>
{
    id <ReceiptCameraOverlayVCDelegate> __weak  delegate;
    BOOL                                        inPreviewMode;
}

@property (nonatomic, weak) id <ReceiptCameraOverlayVCDelegate> delegate;
@property (nonatomic, strong) UIImagePickerController           *imagePickerController;

@property (nonatomic, strong) IBOutlet UIButton                 *btnTakePicture;
@property (nonatomic, strong) IBOutlet UIButton                 *btnRetake;
@property (nonatomic, strong) IBOutlet UIButton                 *btnDone;
@property (nonatomic, strong) IBOutlet UIButton                 *btnAlbum;
@property (nonatomic, strong) IBOutlet UIView                   *vwBottomBar;
@property (nonatomic, strong) IBOutlet UIImageView              *bottomBarImageView;


@property (nonatomic, strong) IBOutlet UIButton                 *btnCancel;
@property (nonatomic, strong) IBOutlet UIButton                 *btnFlash;
@property (nonatomic, strong) IBOutlet UILabel                  *lblTitle; 

- (IBAction)actionCancel:(id)sender;
- (IBAction)actionToggleFlash:(id)sender;

- (IBAction)actionReTakePhoto:(id)sender;
- (IBAction)actionTakePhoto:(id)sender;
- (IBAction)actionDone:(id)sender;

- (void)setupImagePicker:(UIImagePickerControllerSourceType)sourceType;

@end
