//
//  ImagePreviewViewController.h
//  ConcurMobile
//
//  Created by AJ Cram on 4/3/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol ImagePreviewViewControllerDelegate
- (void)pictureConfirmed:(UIImage*)image;
@end

@interface ImagePreviewViewController : MobileViewController<UIAlertViewDelegate>

-(id)initWithImage:(UIImage*)image;

@property (nonatomic, strong) IBOutlet UIImageView              *imageView;

@property (nonatomic, weak) id <ImagePreviewViewControllerDelegate> delegate;

@end
