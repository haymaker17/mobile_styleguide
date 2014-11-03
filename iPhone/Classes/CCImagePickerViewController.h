//
//  CCImagePickerViewController.h
//  TestImagePicker
//
//  Created by Sally Yan on 8/8/13.
//  Copyright (c) 2013 Sally Yan. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ReceiptCameraOverlayView.h"
#import "MobileViewController.h"

@interface CCImagePickerViewController : UIViewController <UIImagePickerControllerDelegate,UINavigationControllerDelegate>{
    
}

@property (strong, nonatomic) UIImagePickerController *picker;

@property BOOL albumSelected;
@property (copy,nonatomic) void (^retake)( CCImagePickerViewController *picker,NSDictionary *info);
@property (copy,nonatomic) void (^expense)( CCImagePickerViewController *picker, NSDictionary *info);
@property (copy,nonatomic) void (^done)( CCImagePickerViewController *picker,NSDictionary *info );
@property (copy,nonatomic) void (^cancel)(CCImagePickerViewController *picker);
@property BOOL isToShowImagePickerView;
-(UIImage*)restrictImageSize:(UIImage*)originalImage;

@end
