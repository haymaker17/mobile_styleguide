//
//  UploadBannerView.m
//  ConcurMobile
//
//  Created by Shifan Wu on 10/29/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "UploadBannerView.h"
#import "UploadQueueViewController.h"

@implementation UploadBannerView
@synthesize delegate = _delegate;
@synthesize lblHeading, iconImg, btn;

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
    }
    return self;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

+ (UploadBannerView *) getUploadView
{
    NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"UploadBannerView" owner:self options:nil];
    for (id oneObject in nib)
    {
        if ([oneObject isKindOfClass:[UploadBannerView class]])
        {
            return oneObject;
        }
    }
	return nil;
}

- (IBAction)btnUploadPressed:(id)sender
{
    if (self.delegate != nil)
    {
        [self.delegate showUploadViewController];
    }
}

- (void) setBannerText:(int) numOfItem
{
    self.lblHeading.text = [NSString stringWithFormat:@"%d %@", numOfItem, [Localizer getLocalizedText:@"items to upload"]];
}
@end
