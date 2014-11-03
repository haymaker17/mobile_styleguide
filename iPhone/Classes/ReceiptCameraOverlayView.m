//
//  ReceiptCameraOverlayView.m
//  TestImagePicker
//
//  Created by Sally Yan on 8/8/13.
//  Copyright (c) 2013 Sally Yan. All rights reserved.
//

#import "ReceiptCameraOverlayView.h"
#import "ImageUtil.h"

@implementation ReceiptCameraOverlayView

@synthesize delegate;

- (id)initWithCoder:(NSCoder *)aDecoder{
    self = [super initWithCoder:aDecoder];
    if (self) {
        self.frame = [[UIScreen mainScreen] bounds];
    }
    return self;
}
- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
    }
    return self;
}


-(void)setUIText
{
// MOB-16301
    self.lblViewTitle.text = [@"Capture Receipt" localize];
    [self.cancelTapped setTitle:[@"Cancel" localize] forState: UIControlStateNormal];
    [self.cancelTapped sizeToFit];
    [self.flashOnOffTapped setTitle:[@"No" localize] forState:UIControlStateNormal];

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
/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

@end
