//
//  UnifiedImagePicker.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 1/14/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol UnifiedImagePickerDelegate <NSObject>
-(void)unifiedImagePickerSelectedImage:(UIImage*)image;
@end

@interface UnifiedImagePicker : UIViewController <UIImagePickerControllerDelegate,UINavigationControllerDelegate> {

}

@property (nonatomic,strong) UIImagePickerController *imagePicker;
@property (nonatomic,weak) id<UnifiedImagePickerDelegate> delegate;
@property (nonatomic,strong) UIImage *originalImage;

+(UnifiedImagePicker*)sharedInstance;
@end
