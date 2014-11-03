//
//  MobileTourVC.m
//  ConcurMobile
//
//  Created by Sally Yan on 2/19/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "MobileTourViewController.h"
#import "MobileTourCollectionViewController.h"

@interface MobileTourViewController ()
@property (weak, nonatomic) IBOutlet UIImageView *concurTourLogo;
@property (weak, nonatomic) IBOutlet UILabel *labelWelcome;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coGapBetweenWelcomeAndLogo;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coWidthForConcurLogoView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coCancelIconWidth;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coCancelIconHeight;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coCancelIconTop;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *coCancelIconRight;

@property (weak, nonatomic) IBOutlet UIImageView *cancelImageView;
@property (weak, nonatomic) IBOutlet UIView *viewForShadowBehindContainer;
@property (strong, nonatomic) MobileTourCollectionViewController *mobileTourCollectionVC;

@end

@implementation MobileTourViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [self setupBackgroundShadow];   // setup the shadow around the container view
    
    self.view.frame = [[UIScreen mainScreen] bounds]; // This is to make sure the view is full-screen, rather than just the container.
    
    // register the TapGestureRecognizer for tapping the exit image
    UITapGestureRecognizer *tapGestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(clickEventOnCancelImage:)];
    tapGestureRecognizer.numberOfTapsRequired = 1;
    [self.cancelImageView addGestureRecognizer:tapGestureRecognizer];
    
    self.labelWelcome.text = [Localizer getLocalizedText:@"Welcome to Concur"];
    
    self.pageControl.currentPageIndicatorTintColor = [UIColor brightBlueConcur];
    
    if([UIDevice isPad]){
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(didRotate:) name:UIDeviceOrientationDidChangeNotification object:nil];
    }
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIDeviceOrientationDidChangeNotification object:nil];
}

- (void)didRotate:(NSNotification *)notification
{
    // the sizes of the containver view and concur logo change when orientaion change
    if (UIDeviceOrientationIsPortrait(self.interfaceOrientation)){

    	// MOB-17649 sets the frame size.  size is messed up.  need to fix this better
        self.view.frame = CGRectMake(0, 0, 768, 1024);

        // size for container view
        self.coContainerWidth.constant = 650.0f;
        self.coContainerHeight.constant = 718.0f;
        
        self.coWidthForConcurLogoView.constant = 435.0f;    // logo size
        
        // size for the cancel image
        self.coCancelIconWidth.constant = 36.0f;
        self.coCancelIconHeight.constant = 36.0f;
        self.coCancelIconTop.constant = -17.0f;
        self.coCancelIconRight.constant = 17.0f;
        
        [self.labelWelcome setFont:[UIFont fontWithName:@"Helvetica Neue" size:28.0f]];
        self.concurTourLogo.image = [UIImage imageNamed:@"Logo-TourIPadPortrait.png"];
    }
    else { // landscape mode
        // size for container view
    	// MOB-17649 sets the frame size.  size is messed up.  need to fix this better
        self.view.frame = CGRectMake(0, 0, 1024, 768);

        self.coContainerWidth.constant = 445.0f;
        self.coContainerHeight.constant = 493.0f;
        
        self.coWidthForConcurLogoView.constant = 350.0f;
        
        // size for the cancel image
        self.coCancelIconWidth.constant = 25.0f;
        self.coCancelIconHeight.constant = 25.0f;
        self.coCancelIconTop.constant = -13.0f;
        self.coCancelIconRight.constant = 13.0f;
        
        [self.labelWelcome setFont:[UIFont fontWithName:@"Helvetica Neue" size:22.0f]];
        self.concurTourLogo.image = [UIImage imageNamed:@"Logo_TourIPadLandscape.png"];
    }
    self.coGapBetweenWelcomeAndLogo.constant = 8.0f;
    [self.view setNeedsUpdateConstraints];
    [self.view layoutIfNeeded];

    // MOB-17649 need to rotate home in the background
    [self.home willRotateToInterfaceOrientation:self.interfaceOrientation duration:0];
}

-(void)setupBackgroundShadow
{
    self.viewForShadowBehindContainer.layer.masksToBounds = NO;
    self.viewForShadowBehindContainer.layer.shouldRasterize = NO;
    self.viewForShadowBehindContainer.layer.shadowOpacity = 1;
}


- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if ([segue.identifier isEqualToString:@"showImagesSegue"]){
        self.mobileTourCollectionVC = segue.destinationViewController;
        __weak MobileTourViewController *weakSelf = self;
        [self.mobileTourCollectionVC setOnPageDidChange:^(NSInteger newPageIndex) {
            if (weakSelf.mobileTourCollectionVC.totalNumberOfPages == 1) {
                [weakSelf.pageControl setHidden:YES];
            }else{
                [weakSelf.pageControl setHidden:NO];
                weakSelf.pageControl.numberOfPages = weakSelf.mobileTourCollectionVC.totalNumberOfPages;
                [weakSelf.pageControl setCurrentPage:newPageIndex];
            }
            
        }];

    }
}

- (void)clickEventOnCancelImage:(id)sender
{
    if(self.onDismissTapped){
        self.onDismissTapped();
    }
}

@end
