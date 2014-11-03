//
//  ImagePreviewViewController.m
//  ConcurMobile
//
//  Created by AJ Cram on 4/3/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "ImagePreviewViewController.h"
#import "ImageUtil.h"
#import "Config.h"

@interface ImagePreviewViewController ()
@end

@implementation ImagePreviewViewController

UIImage* imagePreview;

@synthesize delegate;
@synthesize imageView;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // auto generated
    }
    return self;
}

- (id)initWithImage:(UIImage*)image
{
    self = [self initWithNibName:nil bundle:nil];
    if (self) {
        // TODO: remove magic numbers
        // this limits the view dimensions so popover doesn't auto-scale its height up to the height of the iPad
        self.contentSizeForViewInPopover = CGSizeMake(320,400);

        // store reference to image - to be manipulated in viewDidLoad
        imagePreview = image;

		self.navigationController.navigationBarHidden = NO;
        
        NSString* title = [Localizer getLocalizedText: @"Receipt"];
        self.title = title;
        
        NSString* confirmText = [Localizer getLocalizedText: @"Upload"];
        UIBarButtonItem *confirmButton = [[UIBarButtonItem alloc] initWithTitle:confirmText style:UIBarButtonItemStyleBordered target:self action:@selector(pictureConfirmed)];
        self.navigationItem.rightBarButtonItem = confirmButton;
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    imageView.image = imagePreview;
    [self fitImageViewToFrame:imageView frame:self.view.frame];
    [self centerImageViewInFrame:imageView frame:self.view.frame];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

// return value is modified imageViewOut parameter
// this method is far better than fitToAspect because this fills the parent view more fully
- (void)fitImageViewToFrame:(UIImageView*)imageViewOut frame:(CGRect)frame
{
    if( imageViewOut == nil || imageViewOut.image == nil ||
        frame.size.height <= 0 || frame.size.width <= 0 ||
        imageViewOut.image.size.height <= 0 || imageViewOut.image.size.width <= 0)
    {
        return;
    }
    
    float hScalar = frame.size.height / imageViewOut.image.size.height;
    float wScalar = frame.size.width / imageViewOut.image.size.width;
    
    float scalar = MIN(hScalar, wScalar);
    
    CGSize newImageSize = CGSizeMake( imageViewOut.image.size.width * scalar, imageViewOut.image.size.height * scalar);
    float newImageHeight = MIN(newImageSize.height, self.view.frame.size.height);
    float newImageWidth = MIN(newImageSize.width, self.view.frame.size.width);
    
    imageViewOut.frame = CGRectMake(0, 0, newImageWidth, newImageHeight);
}

// return value is modified imageViewOut parameter
// this method is required because the built in centering code doesn't seem to function properly with the better scaling done above
- (void)centerImageViewInFrame:(UIImageView*)imageViewOut frame:(CGRect)frame
{
    if( imageViewOut == nil || imageViewOut.image == nil ||
       frame.size.height <= 0 || frame.size.width <= 0 ||
       imageViewOut.image.size.height <= 0 || imageViewOut.image.size.width <= 0)
    {
        return;
    }
    
    float xDifference = frame.size.width - imageViewOut.frame.size.width;
    float yDifference = frame.size.height - imageViewOut.frame.size.height;
    float xOrigin = xDifference / 2;
    float yOrigin = yDifference / 2;

    imageViewOut.frame = CGRectMake(xOrigin, yOrigin, imageViewOut.frame.size.width, imageViewOut.frame.size.height);
}

// used a callback/event for upper right button on preview page
- (void)pictureConfirmed
{
    // give the taken picture to our delegate
    [self.delegate pictureConfirmed:imagePreview];
}

@end
