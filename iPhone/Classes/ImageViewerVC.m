//
//  ImageViewerVC.m
//  ConcurMobile
//
//  Created by Paul Kramer on 6/25/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ImageViewerVC.h"
#import "ExSystem.h" 


@implementation ImageViewerVC

@synthesize			scroller;
@synthesize			tb;
@synthesize			imageArray;
@synthesize			pageControl, pagePos;

#pragma mark -
#pragma mark ViewController Methods
- (void)viewWillAppear:(BOOL)animated
{
	[self manipulateImages];
	[super viewWillAppear:animated];
}

// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
	for(UIView *v in scroller.subviews)
		[v removeFromSuperview];
	
	[self manipulateImages];
	[super willRotateToInterfaceOrientation:toInterfaceOrientation duration:duration];
}

/*
 // The designated initializer.  Override if you create the controller programmatically and want to perform customization that is not appropriate for viewDidLoad.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    if ((self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil])) {
        // Custom initialization
    }
    return self;
}
*/


// Implement viewDidLoad to do additional setup after loading the view, typically from a nib.
- (void)viewDidLoad {
	[self setupPage];
	[tb setFrame:CGRectMake(0, 0, self.view.frame.size.width, 30)];
	//self.tb.frame
	//.frame.size = CGRectMake(0, 0, self.frame.size.width, 30);
    [super viewDidLoad];
}



- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}




-(IBAction) closeMe
{
	[self dismissViewControllerAnimated:YES completion:nil];
}

-(void) manipulateImages
{
	float w = 0.0f;
	float h = 0.0f;
	
	if([ExSystem isLandscape])
	{
		w = 480.0f;
		h = 320.0f - 30.0f;
	}
	else {
		w = 320.0f;
		h = 480.0f - 30.0f;
	}
	
	if([UIDevice isPad])
	{
		w = 540.0; //self.view.frame.size.width;
		h = 590.0; //self.view.frame.size.height - 30;
	}

	
	NSInteger imageCount = [imageArray count];
	
	scroller.contentSize = CGSizeMake(imageCount * w, h);
	scroller.pagingEnabled = YES;
	scroller.bounces = YES;
	scroller.indicatorStyle = UIScrollViewIndicatorStyleBlack;
	int iPos = 0;
	
	for(UIImageView *iiV in imageArray)
	//for(UIImage *img in imageArray)
	{
//		float scrollerW = cell.frame.size.width - 14;
//		int w = entry.receiptImage.size.width;
//		int h = entry.receiptImage.size.height;
//		float scaler = (float)w / scrollerW;
//		h = h / scaler;
		UIImage *img = iiV.image;
//		
//		float imgW = img.size.width;
//		float imgH = img.size.height;
//		
//		float scaler = (float)imgW / (float)w;
//		//h = h / scaler;
//		
//		if (imgW > w) {
//			imgW = w;
//			imgH = imgH / scaler;
//		}
//		else if (imgH > h)
//		{
//			imgW = imgW / scaler;
//			imgH = h;
//		}
//		else {
//			imgW = w;
//			imgH = h;
//		}
//
//		
//		//UIImage	*resizedImage = [ImageUtil imageWithImage:img scaledToSize:CGSizeMake(imgW, imgH)]; 
//		
//		int y = 0;
//		
//		if (imgH < h) {
//			y = (h - imgH) / 2;
//		}
//		
		UIImageView *iv = [[UIImageView alloc] initWithFrame:CGRectMake(w * iPos, 0, w, h)];
		
		/////////
		float maxW = w;
		float maxH = h;
		iv.image = [self scaleImageToFit:img MaxW:maxW MaxH:maxH];
		float x = 0.0;
		float y = 0.0;
		float imgW = iv.image.size.width;
		float imgH = iv.image.size.height;
		
		if(imgW < maxW)
			x = ((maxW - imgW) / 2 );
		
		if(imgH < maxH)
			y = ((maxH - imgH) / 2);
		
		iv.frame = CGRectMake((iPos * w) + x, y, imgW, imgH);
		//NSLog(@"x = %f", (iPos * w) + x);
		////////
		//[iv setImage:img];
		[scroller addSubview:iv]; 
		iPos++;
	}
	
	self.pageControl.numberOfPages = [imageArray count];
	
	if(pagePos > 0)
		[scroller setContentOffset:CGPointMake((w * pagePos), 0)];
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
	
//	NSUInteger nimages = 0;
//	CGFloat cx = 0;
//	for (; ; nimages++) {
//		NSString *imageName = [NSString stringWithFormat:@"image%d.jpg", (nimages + 1)];
//		UIImage *image = [UIImage imageNamed:imageName];
//		if (image == nil) {
//			break;
//		}
//		UIImageView *imageView = [[UIImageView alloc] initWithImage:image];
//		
//		CGRect rect = imageView.frame;
//		rect.size.height = image.size.height;
//		rect.size.width = image.size.width;
//		rect.origin.x = ((scrollView.frame.size.width - image.size.width) / 2) + cx;
//		rect.origin.y = ((scrollView.frame.size.height - image.size.height) / 2);
//		
//		imageView.frame = rect;
//		
//		[scrollView addSubview:imageView];
//		[imageView release];
//		
//		cx += scrollView.frame.size.width;
//	}
	
	self.pageControl.numberOfPages = [imageArray count];
	//[scroller setContentSize:CGSizeMake(cx, [scrollView bounds].size.height)];
}

#pragma mark -
#pragma mark UIScrollViewDelegate stuff
- (void)scrollViewDidScroll:(UIScrollView *)_scrollView
{
    if (pageControlIsChangingPage) {
        return;
    }
	
	/*
	 *	We switch page at 50% across
	 */
    CGFloat pageWidth = _scrollView.frame.size.width;
    int page = floor((_scrollView.contentOffset.x - pageWidth / 2) / pageWidth) + 1;
    pageControl.currentPage = page;
}

- (void)scrollViewDidEndDecelerating:(UIScrollView *)_scrollView 
{
    pageControlIsChangingPage = NO;
}

#pragma mark -
#pragma mark PageControl stuff
- (IBAction)changePage:(id)sender 
{
	/*
	 *	Change the scroll view
	 */
    CGRect frame = scroller.frame;
    frame.origin.x = frame.size.width * pageControl.currentPage;
    frame.origin.y = 0;
	
    [scroller scrollRectToVisible:frame animated:YES];
	
	/*
	 *	When the animated scrolling finishings, scrollViewDidEndDecelerating will turn this off
	 */
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

@end
