//
//  UploadBannerView.h
//  ConcurMobile
//
//  Created by Shifan Wu on 10/29/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UploadBannerDelegate.h"

@interface UploadBannerView : UIView
{
    UILabel *lblHeading;
    UIImageView *iconImg;
    UIButton *btn;
}

@property (weak, nonatomic) id <UploadBannerDelegate> delegate;
@property (strong, nonatomic) IBOutlet UIButton *btn;
@property (strong, nonatomic) IBOutlet UIImageView *iconImg;
@property (strong, nonatomic) IBOutlet UILabel *lblHeading;

+ (UploadBannerView *) getUploadView;
- (IBAction)btnUploadPressed:(id)sender;
- (void) setBannerText:(int) numOfItem;

@end
