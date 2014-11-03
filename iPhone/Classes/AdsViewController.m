//
//  AdsViewController.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 12/9/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "AdsViewController.h"
#import "AppsUtil.h"

static AdsViewController *sharedInstance;

@interface AdsViewController (Private)
-(void)setCurrentAdContext;
-(void) adjustFrameForCurrentOrientation;
@end

@implementation AdsViewController
@synthesize adsView, adIndex, adsLookup;

+(AdsViewController*)sharedInstance
{
	if (sharedInstance != nil) 
	{
		return sharedInstance;
	}
	else 
	{
		@synchronized (self)
		{
			if (sharedInstance == nil) 
			{
				sharedInstance = [[AdsViewController alloc] init];
                [sharedInstance setCurrentAdContext];
			}
		}
		return sharedInstance;
	}
}

-(id)init
{
    self = [super init];
	if (self)
	{
        self.adsView = [[UIButton alloc] initWithFrame:CGRectMake(0, 0, 700, 84)];
        [adsView setContentMode:UIViewContentModeScaleAspectFit];
        self.adIndex = 0;
	}
	return self;
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

/*
// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView
{
    
}
*/


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad
{
    [super viewDidLoad];
    self.view = adsView;
    [self adjustFrameForCurrentOrientation];
    
    [self performSelector:@selector(switchAd:) withObject:nil afterDelay:1.0f];
}


- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}


- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [self updateAdsView];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return YES;
}


#pragma mark Actions
-(void) adjustFrameForCurrentOrientation
{
    if ([ExSystem isLandscape]) {
        adsView.frame = CGRectMake(145, 655, 732, 84);
    }
    else
        adsView.frame = CGRectMake(136, 910, 495, 84);
}

-(void) updateAdsView
{
    NSString * img = nil;
    
    NSArray *images = nil;
    if ([ExSystem isLandscape])
        images = adsLookup[@"images_landscape"];
    else
        images = adsLookup[@"images_portrait"];
    
    img = images[adIndex];
    
    //unused variable, removed by AJ 2013-05-06. please delete when appropriate.
    //NSString *selectorString = [[adsLookup objectForKey:@"selectors"] objectAtIndex:adIndex];
    
    [adsView setBackgroundImage:[UIImage imageNamed:img] forState:UIControlStateNormal];
    [adsView addTarget:self action:@selector(selectorString) forControlEvents:UIControlEventTouchUpInside];
    [self adjustFrameForCurrentOrientation];
}

-(void)switchAd:(NSTimer*)theTimer
{	
    if (adsLookup != nil && [adsLookup count] > 0) 
    {
        adIndex++;
        adIndex = (adIndex%[adsLookup count]);
        [self updateAdsView];
    }
}

- (void)buttonTripItPressed
{
    [AppsUtil launchTripItApp];
}

- (void)buttonTaxiPressed
{
    [AppsUtil launchTaxiMagicApp];
}

-(void)setCurrentAdContext
{
    if (adsLookup == nil) {
        self.adsLookup = [[NSMutableDictionary alloc] init];
    }
	
    NSMutableArray *selectorArray = [[NSMutableArray alloc] init];
    
    if ([[ExSystem sharedInstance] hasRole:ROLE_TRIPITAD_USER])
    {
        [selectorArray addObject:@"buttonTripItPressed"];
    }
    [selectorArray addObject:@"buttonTaxiPressed"];

    NSMutableArray *landscapeImgs = [[NSMutableArray alloc] init];
    
    if ([[ExSystem sharedInstance] hasRole:ROLE_TRIPITAD_USER])
    {
        [landscapeImgs addObject:@"tripit_ad_ipad_landscape"];
    }
    [landscapeImgs addObject:@"ad_taxi_landscape"];
    
    NSMutableArray *portraitImgs = [[NSMutableArray alloc] init];
    
    if ([[ExSystem sharedInstance] hasRole:ROLE_TRIPITAD_USER])
    {
        [portraitImgs addObject:@"tripit_ad_ipad_portrait"];
    }
    [portraitImgs addObject:@"ad_taxi_portrait"];
    
    adsLookup[@"images_landscape"] = landscapeImgs;
    adsLookup[@"images_portrait"] = portraitImgs;
    
	
	adsLookup[@"selectors"] = selectorArray;
	
}

@end
