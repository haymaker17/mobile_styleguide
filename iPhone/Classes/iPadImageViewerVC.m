    //
//  iPadImageViewerVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 9/27/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "iPadImageViewerVC.h"
#import "ExSystem.h" 


@implementation iPadImageViewerVC

@synthesize			scroller;
@synthesize			tb;
@synthesize			imageArray;
@synthesize			pageControl, pagePos;


#pragma mark -
#pragma mark MVC Methods
-(void)respondToFoundData:(Msg *)msg
{//respond to data that might be coming from the cache
	
	
	if ([msg.idKey isEqualToString:IMAGE] && msg.parameterBag != nil)
	{

		if((msg.parameterBag)[@"IMAGE_VIEW"] != nil)
		{
			UIImage *gotImg = [[UIImage alloc] initWithData:msg.data];
			//img = gotImg;
			UIImageView *iv = (msg.parameterBag)[@"IMAGE_VIEW"];
			iv.image = gotImg;
		}
	}
	else if ([msg.idKey isEqualToString:VENDOR_IMAGE] && msg.parameterBag != nil)
	{//segment should already be set
		if ((msg.parameterBag)[VENDOR_IMAGE] != nil)
		{

		}
		
	}
}


#pragma mark -
#pragma mark View Controller Methods
// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
    [super viewDidLoad];
	
	isThumbs = YES;
    
    self.title = [Localizer getLocalizedText:@"View Images"];
    if (![ExSystem is7Plus])
        self.tb.tintColor = [UIColor darkBlueConcur_iOS6];
    [self makeToolBarSingle];

	[self manipulateImages];
}



- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

-(void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	if(![UIDevice isPad])
	{
		if(isThumbs)
			[self manipulateImages];
		else 
			[self manipulateImagesOG];
	}
}


- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
	NSLog(@"iPadImageViewerVC Low Memory");
    [super didReceiveMemoryWarning];

}


- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
    self.scroller = nil;
    self.tb = nil;
    self.pageControl = nil;
}

-(void)viewDidLayoutSubviews
{
    [super viewDidLayoutSubviews];
    if ([self respondsToSelector:@selector(topLayoutGuide)])
    {
        CGRect viewBounds = self.view.bounds;
        CGFloat topBarOffset = self.topLayoutGuide.length;
        if (self.view.frame.origin.y != topBarOffset)
            [self.view setFrame:CGRectMake(viewBounds.origin.x, topBarOffset, viewBounds.size.width, viewBounds.size.height - topBarOffset)];
    }
    if([UIDevice isPad])
    {
        // On iPad when you present two modal forms, one from within the other, the second modal seems
        // to given a priority over the first. This doesn't appear as a problem until you rotate the
        // display. When you do that, the two modals rotate, but the first resizes to fill the screen behind the
        // second - and share's the modal state with the first. So you are able to click out of the second modal and
        // interact with the first modal.
        // To stop this happening you have to change the modalPresentationStyle of the second modal after it has
        // been presented.
        // This must be a bug of some sort introduced in IOS6, IOS5 did not have the issue.
        // This problem does not affect iPhone or iPod, hence why we check for iPad only.
        // Solution was found at:
        //  http://iphonedevsdk.com/forum/iphone-sdk-development/108214-two-modal-uiviewcontrollers-displayed-as-uimodalpresentationformsheet-on-ipad-broke-when-ipad.html
        [self setModalPresentationStyle: UIModalPresentationPageSheet];
    }
}



#pragma mark -
#pragma mark View Controller Management
-(IBAction) closeMe:(id)sender
{
	[self dismissViewControllerAnimated:YES completion:nil];
    self.imageArray = nil; // MOB-9822 Release all images
}


#pragma mark -
#pragma mark Fiddle With the Layout
-(void) manipulateImages
{
	float w = 0.0f;
	float h = 0.0f;
	float imgThumbW = 100.0;
	float imgThumbH = 100.0;
	int rowSize = 5;
	//int gutter = 10;
	int margin = 10;
	
	if([UIDevice isPad])
	{
		w = 540.0; //self.view.frame.size.width;
		h = 590.0; //self.view.frame.size.height - 30;
	}
	else 
	{
		if([ExSystem isLandscape])
		{
			w = 480;
			h = 320;
			rowSize = 4;
			margin = 40;
		}
		else 
		{
			w = 320;
			h = 480;
			rowSize = 3;
		}
	}

	
	//int imageCount = [imageArray count];
	
    for(UIView *v in scroller.subviews)
        [v removeFromSuperview];
	
	scroller.pagingEnabled = NO;
	scroller.bounces = YES;
	scroller.indicatorStyle = UIScrollViewIndicatorStyleBlack;
	int iPos = 0;
	int iRow = 0;
	[pageControl setHidden:YES];
	
	
	for(UIImageView *iiV in imageArray)
	{

		UIImage *img = iiV.image;
		UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(w * iPos, 0, w, h)];
		
		float maxW = imgThumbW - 10;
		float maxH = imgThumbH - 10;
		iv.image = [self scaleImageToFit:img MaxW:maxW MaxH:maxH];
		[iv setContentMode:UIViewContentModeCenter];
		float x = margin;
		float y = 20.0;

		float imgW = iv.image.size.width;
		float imgH = iv.image.size.height;
		
		if(imgW < maxW)
			x = (((maxW - imgW) + x) / 2 );
		
		if(imgH < maxH)
			y = (((maxH - imgH) + y) / 2);
		
		iv.frame = CGRectMake((iPos * imgThumbW) + x, (imgThumbH * iRow) + y, imgThumbW, imgThumbH);// imgW, imgH);
		UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
		if(iRow == 0)
			btn.tag = iPos;
		else 
			btn.tag = (iRow * rowSize) + iPos;
		btn.frame = CGRectMake((iPos * imgThumbW) + x, (imgThumbH * iRow) + y, imgW, imgH); 
		
		[btn addTarget:self action:@selector(switchToSingleView:) forControlEvents:UIControlEventTouchUpInside];
		
		UILabel *lblBack = [[UILabel alloc] initWithFrame:CGRectMake(((iPos * imgThumbW) + x + ((imgThumbW - imgW)/2)) - 2, ((imgThumbH * iRow) + y+ ((imgThumbH - imgH)/2)) - 2, imgW + 4, imgH + 4)];
		lblBack.backgroundColor = [UIColor whiteColor];
		lblBack.text = @"";

		[scroller addSubview:lblBack];
		[scroller addSubview:iv]; 
		[scroller addSubview:btn];
		iPos++;
		if (iPos > 4 && [UIDevice isPad]) 
		{
			iPos = 0;
			iRow++;
		}
		else if (iPos > 2 && ![UIDevice isPad] && ![ExSystem isLandscape]) 
		{
			iPos = 0;
			iRow++;
		}
		else if (iPos > 3 && ![UIDevice isPad] && [ExSystem isLandscape]) 
		{
			iPos = 0;
			iRow++;
		}
	}
	
	scroller.contentSize = CGSizeMake(w, ((imgThumbH + 20) * (iRow + 3)));
	
	self.pageControl.numberOfPages = [imageArray count];
	
	if(pagePos > 0)
		[scroller setContentOffset:CGPointMake((w * pagePos), 0)];
}


-(void) manipulateImagesOG
{
    for(UIView *v in scroller.subviews)
        [v removeFromSuperview];
	
	self.pageControl.numberOfPages = 0;
	
	float w = 540.0f;
	float h = 590.0f;
	float imgThumbW = 540.0;
	float imgThumbH = 590.0;
	
	if(![UIDevice isPad]) 
	{
		if([ExSystem isLandscape])
		{
			w = 480;
			h = 320;
			imgThumbW = w;
			imgThumbH = h - 44;
		}
		else 
		{
			w = 320;
			h = 480;
			imgThumbW = w;
			imgThumbH = h - 44;
		}
		
	}
	
	int imageCount = [imageArray count];
	
	scroller.contentSize = CGSizeMake(imageCount * w, h);
	scroller.pagingEnabled = YES;
	scroller.bounces = YES;
	scroller.multipleTouchEnabled = YES;
	scroller.indicatorStyle = UIScrollViewIndicatorStyleBlack;
	int iPos = 0;
	[pageControl setHidden:NO];
	
	for(UIImageView *iiV in imageArray)
	{
		
		UIImage *img = iiV.image;
		TouchIV *iv = [[TouchIV alloc] initWithFrame:CGRectMake(w * iPos, 0, w, h)];
		[iv setUserInteractionEnabled:YES];
		[iv setMultipleTouchEnabled:YES];
		iv.parentVC = self;

		float maxW = imgThumbW; 
		float maxH = imgThumbH; 
		iv.image = [self scaleImageToFit:img MaxW:maxW MaxH:maxH];
		float x = 0.0;
		float y = 0.0;
		float imgW = iv.image.size.width;
		float imgH = iv.image.size.height;
		
		if(imgW < maxW)
			x = (((maxW - imgW) + x) / 2 );
		
		if(imgH < maxH)
			y = (((maxH - imgH) + y) / 2);
		
		iv.frame = CGRectMake((iPos * imgThumbW) + x, y, imgW, imgH);

		[scroller addSubview:iv]; 
		iPos++;
	}
	
	self.pageControl.numberOfPages = [imageArray count];
	
	if(pagePos > 0)
		[scroller setContentOffset:CGPointMake((w * pagePos), 0)];
}


-(IBAction) switchToSingleView:(id)sender
{
	isThumbs = NO;
	
	for(UIView *v in scroller.subviews)
		[v removeFromSuperview];
	
	UIButton *btn = (UIButton *)sender;
	pagePos = btn.tag;
	[self manipulateImagesOG];
	[self makeToolBarGrid];

}

-(IBAction) switchToGridView:(id)sender
{
	isThumbs = YES;
	
	for(UIView *v in scroller.subviews)
		[v removeFromSuperview];
	
	[scroller setContentOffset:CGPointMake(0, 0)];
	
	pagePos = 0;
	[self manipulateImages];
	[self makeToolBarSingle];

}


#pragma mark - Toolbar Makers
-(void) makeToolBarSingle
{
	UIBarButtonItem *btnDone = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_DONE_BTN"] 
																style:UIBarButtonItemStyleBordered 
															   target:self 
															   action:@selector(closeMe:)];
	UIBarButtonItem *btnSingleImage = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_coverflow.png"] style:UIBarButtonItemStyleBordered target:self action:@selector(switchToSingleView:)];
    
    UIBarButtonItem *btnTitle = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"View Images"] style:UIBarButtonItemStylePlain target:nil action:nil];
	UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    if ([ExSystem is7Plus])
    {
        btnDone.tintColor = [UIColor darkBlueConcur_iOS6];
        btnSingleImage.tintColor = [UIColor darkBlueConcur_iOS6];
    }
	NSArray *toolbarItems = @[btnDone, flexibleSpace, btnTitle, flexibleSpace, btnSingleImage];
	[tb setItems:toolbarItems];
}

-(void) makeToolBarGrid
{
	UIBarButtonItem *btnDone = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"LABEL_DONE_BTN"] 
																style:UIBarButtonItemStyleBordered 
															   target:self 
															   action:@selector(closeMe:)];
	UIBarButtonItem *btnGrid = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"toolbars_grid.png"] style:UIBarButtonItemStyleBordered target:self action:@selector(switchToGridView:)];
	
    UIBarButtonItem *btnTitle = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"View Images"] style:UIBarButtonItemStylePlain target:nil action:nil];
    
    UIBarButtonItem *flexibleSpace = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    if ([ExSystem is7Plus])
    {
        btnDone.tintColor = [UIColor darkBlueConcur_iOS6];
        btnGrid.tintColor = [UIColor darkBlueConcur_iOS6];
    }
	NSArray *toolbarItems = @[btnDone, flexibleSpace, btnTitle, flexibleSpace, btnGrid];
	[tb setItems:toolbarItems];
}

#pragma mark -
#pragma mark The Guts
- (void)setupPage
{
	scroller.delegate = self;
	
	[self.scroller setBackgroundColor:[UIColor blackColor]];
	[scroller setCanCancelContentTouches:NO];
	
	scroller.indicatorStyle = UIScrollViewIndicatorStyleWhite;
	scroller.clipsToBounds = YES;
	scroller.scrollEnabled = YES;
	scroller.pagingEnabled = YES;
	
	self.pageControl.numberOfPages = [imageArray count];

}

#pragma mark -
#pragma mark UIScrollViewDelegate stuff
- (void)scrollViewDidScroll:(UIScrollView *)_scrollView
{
    if (pageControlIsChangingPage) {
        return;
    }
	
    CGFloat pageWidth = _scrollView.frame.size.width;
    int page = floor((_scrollView.contentOffset.x - pageWidth / 2) / pageWidth) + 1;
    pageControl.currentPage = page;
	pagePos = page;
}

- (void)scrollViewDidEndDecelerating:(UIScrollView *)_scrollView 
{
    pageControlIsChangingPage = NO;
}

#pragma mark -
#pragma mark PageControl stuff
- (IBAction)changePage:(id)sender 
{
    CGRect frame = scroller.frame;
    frame.origin.x = frame.size.width * pageControl.currentPage;
    frame.origin.y = 0;
	
    [scroller scrollRectToVisible:frame animated:YES];

    pageControlIsChangingPage = YES;
}


-(UIImage *)scaleImageToFit:(UIImage *) img MaxW:(float)maxW MaxH:(float)maxH
{
	int w = img.size.width;
	int h = img.size.height;
	float scaler = (float)w / maxW;
	if(w <= maxW && h > maxH)
	{
		scaler = (float)h / maxH;
		w = w / scaler;
		return [ImageUtil imageWithImage:img scaledToSize:CGSizeMake(w, maxH)];
	}
	else 
	{
		h = h / scaler;
		if(h > maxH)
		{
			scaler = (float)h / maxH;
			w = maxW / scaler;
			return [ImageUtil imageWithImage:img scaledToSize:CGSizeMake(w, maxH)];
		}
		else 
			return [ImageUtil imageWithImage:img scaledToSize:CGSizeMake(maxW, h)];//[[UIImage alloc] initWithData:mydata];
	}
}


#pragma mark -
#pragma mark Load Hotel Methods
-(void)loadHotelImages:(NSMutableArray *)aImageURLs
{	
	float w = 211; 
	float h = 243; 
	self.imageArray = [[NSMutableArray alloc] initWithObjects:nil];
	
	for(NSString *imageURL in aImageURLs)
	{
		UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, w, h)];
		//do things to load the actual image
		UIImage *img = [UIImage imageNamed:@"LoadingImage.png"];
		[iv setImage:img];
		[[ExSystem sharedInstance].imageControl getImageAsynchForImageMVC:imageURL RespondToImage:img IV:iv MVC:self]; //firing off the fetch, loads the image into the imageview
		[imageArray addObject:iv]; //this is the only iv that we want to keep around...
	}
}
@end
