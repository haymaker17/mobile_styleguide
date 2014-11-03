//
//  UploadQueueNonCancellableAlertView.m
//  ConcurMobile
//
//  Created by charlottef on 11/18/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "UploadQueueNonCancellableAlertView.h"
#import "UploadQueue.h"

@implementation UploadQueueNonCancellableAlertView

@synthesize UISpinner, showSpinner;

-(id) initWithTitle:(NSString*)title message:(NSString*)message
{
    self = [super initWithTitle:title message:message delegate:self cancelButtonTitle:nil otherButtonTitles:nil];
    if (self)
    {
        self.showSpinner = YES;
        self.UISpinner = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhite];
    }
    return self;
}

#pragma mark -
#pragma mark UIAlertView Methods
-(void)show
{
    // Check the queue.  If it's back to idle then don't bother, otherwise make this the delegate
    UploadQueue *uploadQueue = [UploadQueue sharedInstance];
    
    if (uploadQueue.state == UploadQueueStateCancelling || [uploadQueue isUpdatingAfterUpload])
    {
        
        uploadQueue.delegate = self;
    
        [super show];
        
        if (showSpinner)
        {
            //self.UISpinner.center = CGPointMake(self.bounds.size.width * 0.23f, self.bounds.size.height * 0.41f);
            [self.UISpinner startAnimating];
            [self addSubview:UISpinner];
        }
    }
}

#pragma mark - UIAlertViewDelegate
-(void)willPresentAlertView:(UIAlertView *)alertView
{
    [super willPresentAlertView:alertView];
    
    //if placing the activity indicator failed in show function
    //center point of UIalerView for UIspinner (142.000000 54.400002)
    if (self.UISpinner.center.x != 142.000000 || self.UISpinner.center.y != 54.400002)
    {
        self.UISpinner.center = CGPointMake(self.bounds.size.width * 0.5f, self.bounds.size.height * 0.33f);
    }
}

#pragma mark -
#pragma mark UploadQueueDelegate Methods
-(void) willUploadQueue{}
-(void) didUploadQueue:(BOOL)wasUploadCancelled
{
    [self dismissWithClickedButtonIndex:0 animated:NO];
}
-(void) didCancelUploadQueue{}
-(void) willUploadItemNumber:(int)itemNumber totalItems:(int)totalItems{}
-(void) didUploadItemNumber:(int)itemNumber totalItems:(int)totalItems{}
-(void) didFailToUploadItemNumber:(int)itemNumber totalItems:(int)totalItems{}
-(void) willUpdateAfterUpload:(BOOL)wasUploadCancelled{}
-(void) didUpdateAfterUpload:(BOOL)wasUploadCancelled{}

-(void) didForceQuitUpload
{
    [self dismissWithClickedButtonIndex:0 animated:NO];
}

@end
