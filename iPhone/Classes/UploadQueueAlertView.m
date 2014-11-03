//
//  UploadQueueAlertView.m
//  ConcurMobile
//
//  Created by Shifan Wu on 11/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "UploadQueueAlertView.h"
#import "UploadQueue.h"
#import "UploadQueueNonCancellableAlertView.h"

@implementation UploadQueueAlertView
@synthesize UISpinner, showSpinner, needUpdateMsg;
@synthesize didClickCancelUploadBtn;
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

-(id) initForUpload
{
    self = [super initWithTitle:[Localizer getLocalizedText:@"Uploading Please Wait"] message:@"\n\n" delegate:self cancelButtonTitle:[Localizer getLocalizedText:@"Cancel Upload"] otherButtonTitles:nil];
    if (self)
    {
        self.showSpinner = YES;
        self.needUpdateMsg = YES;
        self.UISpinner = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhite];
    }
    return self;
}

#pragma mark -
#pragma mark UIAlertView Methods
-(void)show
{
    [super show];
    
    [UploadQueue sharedInstance].delegate = self;
    if (showSpinner)
    {
        //self.UISpinner.center = CGPointMake(self.bounds.size.width * 0.23f, self.bounds.size.height * 0.41f);
        self.UISpinner.center = CGPointMake(self.bounds.size.width * 0.5f, self.bounds.size.height * 0.33f);
        [self.UISpinner startAnimating];
        [self addSubview:UISpinner];
    }
}

-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    if (buttonIndex == 0) // Cancel button
    {
        NSDictionary *dict = @{@"How many uploaded": @"0", @"Canceled From": @"Uploading View"};
        [Flurry logEvent:@"Offline: Upload Cancel" withParameters:dict];
        self.didClickCancelUploadBtn = YES;
    }
}

-(void)alertView:(UIAlertView *)alertView didDismissWithButtonIndex:(NSInteger)buttonIndex
{
    NSString *title = nil;
    
    if (buttonIndex == 0 && didClickCancelUploadBtn) // wasUploadCanceled == YES
    {
        // Now cancel the upload
        [UploadQueue sharedInstance].delegate = nil;
        [[UploadQueue sharedInstance] cancelUpload];

        // Tell the user we're stopping the upload
        title = [Localizer getLocalizedText:@"Stopping Upload"];
    }
    else
    {
        // Bring up a new alert that either says were updating
        title = [Localizer getLocalizedText:@"Updating"];
    }
    
    [UploadQueue sharedInstance].delegate = nil; // Don't call me back, it will be a moment before the next alert is ready.  It will make itself the delegate as necessary
    [self performSelector:@selector(invokeNonCancellableAlertView:) withObject:title afterDelay:0.5f];
}

-(void) invokeNonCancellableAlertView:(NSString*)title
{
    NSString *message =[NSString stringWithFormat:@"\n%@", [Localizer getLocalizedText:@"Please Wait"]];
    UploadQueueNonCancellableAlertView *alertView = [[UploadQueueNonCancellableAlertView alloc] initWithTitle:title message:message];
    [alertView show];
}

#pragma mark -
#pragma mark UploadQueueDelegate Methods
-(void) willUploadQueue{}
-(void) didUploadQueue:(BOOL)wasUploadCancelled{}
-(void) didCancelUploadQueue{}
-(void) willUploadItemNumber:(int)itemNumber totalItems:(int)totalItems
{
    NSString *updateNumberStatus = [NSString stringWithFormat:[Localizer getLocalizedText:@"Uploading count"], itemNumber+1, totalItems];
    NSString *alertText = [NSString stringWithFormat:@"\n%@",updateNumberStatus];
    [self setMessage:alertText];
    [self layoutIfNeeded];      // force to refresh the alert view message
    
    
}
-(void) didUploadItemNumber:(int)itemNumber totalItems:(int)totalItems{}
-(void) didFailToUploadItemNumber:(int)itemNumber totalItems:(int)totalItems{}

-(void) willUpdateAfterUpload:(BOOL)wasUploadCancelled
{
    if (!wasUploadCancelled)
        [self dismissWithClickedButtonIndex:1 animated:NO]; // Dismiss uploading alert when all items uploaded without hitting 'cancel' button
    else
    {
        self.didClickCancelUploadBtn = YES;
        [self dismissWithClickedButtonIndex:0 animated:NO]; // should not get called. Add for safety so user won't stuck on an alert view.
    }
}

-(void) didUpdateAfterUpload:(BOOL)wasUploadCancelled{}

-(void) didForceQuitUpload
{
    [self dismissWithClickedButtonIndex:0 animated:NO];
}

@end
