//
//  SegDetailCellPad.m
//  ConcurMobile
//
//  Created by Paul Kramer on 5/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "SegDetailCellPad.h"
#import "SegDetailRowCellPad.h"
#import "ViewConstants.h"
#import "AirCellPad.h"
#import "ItinDetailsCellLabel.h"
#import "FormatUtils.h"
#import "ImageViewerVC.h"
#import "ItinDetailsCellInfo.h"
#import "AppsUtil.h"

@implementation SegDetailCellPad
@synthesize scroller, tableDetails, iv, tb, pager;
@synthesize detail, dVC, aDetails, rootVC, segment, details, ivVendor;
@synthesize		imageArray;
@synthesize		pageControl, hotelImagesArray, lblBackground, lblBack1, lblBack2;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if ((self = [super initWithStyle:style reuseIdentifier:reuseIdentifier])) {
        // Initialization code
    }
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {

    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}





#pragma mark -
#pragma mark Utility Menus
-(IBAction)goSomeplace:(id)sender
{
	UIButton *btn = (UIButton *)sender;
	int aPos = btn.tag;
	detail = aDetails[aPos];
	
	NSString *mapAddress = detail.mapAddress;
	NSString *vendorName = detail.codeName;
	//NSString *vendorCode = detail.code;
	MapViewController *mapView = [[MapViewController alloc] init];
	mapView.mapAddress = mapAddress;
	mapView.anoTitle = vendorName;
	mapView.anoSubTitle = mapAddress;
	//NSLog(@"mapAddress = %@", mapAddress);
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 30200
	mapView.modalPresentationStyle = UIModalPresentationFormSheet;
#endif
	
	[dVC presentViewController:mapView animated:YES completion:nil]; 
	
}


-(void)callNumber:(id)sender
{
	//(NSString *)phoneNum
	//[[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", phoneNum]]];
}


-(IBAction)loadWebView:(id)sender
{
	//(NSString *)specialValueWeb WebViewTitle:(NSString *)webViewTitle
	//do web view
	UIButton *btn = (UIButton *)sender;
	int aPos = btn.tag;
	detail = aDetails[aPos];
	WebViewController *webView = [[WebViewController alloc] init];
	webView.rootViewController = rootVC;
	webView.url = [NSString stringWithFormat:@"https://%@", detail.url];
	webView.viewTitle = detail.detailTitle;
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 30200
	webView.modalPresentationStyle = UIModalPresentationFormSheet;
#endif
	[dVC presentViewController:webView animated:YES completion:nil]; 
	
}


#pragma mark -
#pragma mark Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    // Return the number of sections.
    //return <#number of sections#>;
	return 1;
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    //return <#number of rows in section#>;
	return [details count];
}


// Customize the appearance of table view cells.
- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    int row = indexPath.row;
	
	//NSMutableArray *sectionData = nil;
	SegmentRow *segRow = nil;
	
	if([segment.type isEqualToString:SEG_TYPE_RAIL] || [segment.type isEqualToString:SEG_TYPE_DINING] || [segment.type isEqualToString:SEG_TYPE_AIR])
	{
		//sectionData = [details objectAtIndex:row];
		segRow = details[row];
	}
	
    static NSString *CellIdentifier = @"SegDetailRowCellPad";

	if([segment.type isEqualToString:SEG_TYPE_AIR] && row == 0)
	{//do the air row...
		AirCellPad *cell = (AirCellPad *)[tableView dequeueReusableCellWithIdentifier: @"AirCellPad"];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"AirCellPad" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[AirCellPad class]])
					cell = (AirCellPad *)oneObject;
		}
		
		cell.rootVC = rootVC;
		cell.dVC = dVC;
		
		//DEPARTURE
        //Departure Time and Date
        NSMutableString *departTime = [NSMutableString string];
        NSMutableString *departDate = [NSMutableString string];
        [SegmentData getDepartTimeString:segment timeStr:departTime dateStr:departDate];
        
		cell.lblDepartTime.text = departTime;

		NSString *airportURL = [[ExSystem sharedInstance] getURLMap:@"AIRPORTS" LocalConstant:segment.relStartLocation.cityCode];

		cell.lblDepartAirport.text = [SegmentData getAirportFullName:segment.relStartLocation];
        //[NSString stringWithFormat:@"(%@) %@, %@", segment.relStartLocation.cityCode, segment.relStartLocation.airportName, segment.relStartLocation.AirportState];
		cell.urlDepartAir = airportURL;
		
        NSMutableString *term = [NSMutableString string];
        NSMutableString *gate = [NSMutableString string];
        
        [SegmentData getDepartTermGate:segment terminal:term gate:gate];

		cell.lblDepartTerminalGate.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Terminal t Gate t"], term, gate];
		cell.urlDepartTermGate = airportURL;
		
		//ARRIVAL
        //Arrival Time and Date
        NSMutableString *arriveTime = [NSMutableString string];
        NSMutableString *arriveDate = [NSMutableString string];
        [SegmentData getArriveTimeString:segment timeStr:arriveTime dateStr:arriveDate];
		cell.lblArriveTime.text = arriveTime;
		airportURL = [[ExSystem sharedInstance] getURLMap:@"AIRPORTS" LocalConstant:segment.relStartLocation.cityCode];

		cell.lblArriveAirport.text = [SegmentData getAirportFullName:segment.relEndLocation];
        //[NSString stringWithFormat:@"(%@) %@, %@", segment.endCityCode, segment.endAirportName, segment.endAirportState];
		cell.urlArriveAir = airportURL;
		
        term = [NSMutableString string];
        gate = [NSMutableString string];
        
        [SegmentData getArriveTermGate:segment terminal:term gate:gate];

		cell.lblArriveTerminalGate.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Terminal t Gate t"], term, gate];
		cell.urlArriveTermGate = airportURL;

		return cell;
	}
	else if([segRow.specialCellType isEqualToString:@"RAIL"])
	{
		TrainBookingDetailCell *cell = (TrainBookingDetailCell*)[tableView dequeueReusableCellWithIdentifier:@"TrainBookingDetailCell"];
		if (cell == nil)
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"TrainBookingDetailCell" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[TrainBookingDetailCell class]])
					cell = (TrainBookingDetailCell *)oneObject;
		}
		
		NSString *railStation = [SegmentData getRailStation:segment.relStartLocation];
		
		NSString *endRailStation = [SegmentData getRailStation:segment.relEndLocation];
		
		cell.lblDepartureDate.text = [DateTimeFormatter formatDateMedium:segment.relStartLocation.dateLocal];
		cell.lblDepartureTime.text = [DateTimeFormatter formatTimeForTravel:segment.relStartLocation.dateLocal];
		cell.lblDepartureStation.text = railStation;
		cell.lblArrivalTime.text = [DateTimeFormatter formatTimeForTravel:segment.relEndLocation.dateLocal];
		cell.lblArivalDate.text = [DateTimeFormatter formatDateMedium:segment.relEndLocation.dateLocal];
		cell.lblArrivalStation.text = endRailStation; // train.arrAirp;
		cell.lblDuration.text = [FormatUtils formatDuration:[segment.duration intValue]];
		cell.lblSeat.text = [NSString stringWithFormat:@"Seat Class: %@-%@", segment.classOfService, segment.cabin];
		cell.lblAmount.text = [Localizer getLocalizedText:@"Departing Train"]; // [FormatUtils formatMoney:segment.totalRate crnCode:@"USD"];
		cell.lblTrain.text = [NSString stringWithFormat:[Localizer getLocalizedText:@"Train: token"], segment.trainNumber];
		return cell;
	}
	else if([segRow.specialCellType isEqualToString:@"DINING"])
	{
		ItinDetailsCellInfo *cell = (ItinDetailsCellInfo *)[tableView dequeueReusableCellWithIdentifier: INFO_NIB];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:INFO_NIB owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[ItinDetailsCellInfo class]])
					cell = (ItinDetailsCellInfo *)oneObject;
		}
		
		NSString *location = [SegmentData getMapAddress:segment.relStartLocation];
		cell.labelMap.text = [Localizer getLocalizedText:@"ITIN_DETAILS_VIEW_MAP"];
		//cell.mapAddress = [NSString stringWithFormat:@"%@\n%@, %@, %@", segment.startAddress, segment.startCity, segment.startState, segment.startPostalCode];
		cell.labelAddress1.text = location; 
		cell.labelAddress2.text = location;
		
		cell.mapAddress = location;
		cell.rootVC = rootVC;
		//cell.idVC = self;
		cell.vendorCode = segment.vendor;
		cell.vendorName = segment.segmentName;
		
		UIImage *gotImg = [UIImage imageNamed:@"fakeDining.png"]; //todo: hard coded place holder.  Right now we have no images to insert here
		[cell.imgVendor setImage:gotImg];
		
		return cell;
	}
	else if([segment.type isEqualToString:SEG_TYPE_RAIL] || [segment.type isEqualToString:SEG_TYPE_DINING] || [segment.type isEqualToString:SEG_TYPE_AIR])
	{
		ItinDetailsCellLabel *cell = (ItinDetailsCellLabel *)[tableView dequeueReusableCellWithIdentifier: LABEL_NIB];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:LABEL_NIB owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[ItinDetailsCellLabel class]])
					cell = (ItinDetailsCellLabel *)oneObject;
		}
		
		[cell.btn1 setHidden:YES];
		[cell.btn2 setHidden:YES];
		[cell.labelValue1 setHidden:YES];
		[cell.labelValue2 setHidden:YES];
		[cell.btn1 setHidden:YES];
		//cell.rootVC = rootViewController;
		//cell.idVC = self;
		
		cell.labelLabel.text = segRow.rowLabel;
		cell.labelValue.text = segRow.rowValue;
		
		if(segRow.isWeb)
			cell.ivIcon.image = [UIImage imageNamed:@"www_or_link_24X24.png"];
		
		if(segRow.isPhone)
			cell.ivIcon.image = [UIImage imageNamed:@"action_phone.png"];
		
		if(segRow.isMap)
			cell.ivIcon.image = [UIImage imageNamed:@"action_map.png"];
		
		[cell.labelValue setTextAlignment:NSTextAlignmentLeft];
		
		if(segRow.isMap || segRow.isWeb || segRow.isMap)
		{
			[cell.labelLabel setHidden:YES];
			cell.labelValue.textColor = [UIColor blueColor];
			[cell.labelValue setFont:[UIFont systemFontOfSize:15.0f]];
			[cell setAccessoryType:UITableViewCellAccessoryDisclosureIndicator];
		}
		else if(segRow.isEmblazoned)
		{
			[cell.labelValue setFont:[UIFont boldSystemFontOfSize:16.0f]];
			cell.labelValue.textColor = [UIColor redColor];
			[cell setAccessoryType:UITableViewCellAccessoryNone];
			[cell.labelValue setTextAlignment:NSTextAlignmentRight];
		}
		else {
			[cell.labelValue setFont:[UIFont systemFontOfSize:15.0f]];
			cell.labelValue.textColor = [UIColor blackColor];
			[cell setAccessoryType:UITableViewCellAccessoryNone];
		}
		
		if(segRow.isVendorRow)
		{
//			UIImage *gotImg = [[ExSystem sharedInstance].imageControl getVendorImageAsynchForImageView:segment.vendor VendorType:segRow.vendorType RespondToIV:cell.imgView];
//			cell.imgView.image = gotImg;
			cell.imgView.hidden = NO;
			cell.ivIcon.hidden = YES;
			
			cell.labelLabel.hidden = YES;
			cell.labelValue.hidden = YES;
			if([segment.type isEqualToString:SEG_TYPE_DINING])
				cell.labelVendor.text = segment.segmentName;
			else 
				cell.labelVendor.text = segment.vendorName;
			cell.labelVendor.hidden = NO;
			//cell.labelVendor.frame = CGRectMake(40, 0, cell.labelVendor.frame.size.width, 44);
		}
		else 
		{
			if(segRow.isMap || segRow.isWeb || segRow.isPhone)
			{
				cell.labelLabel.hidden = YES;
				cell.ivIcon.hidden = NO;
			}
			else 
			{
				cell.labelLabel.hidden = NO;
				cell.ivIcon.hidden = YES;
			}
			cell.labelValue.hidden = NO;
			cell.labelVendor.hidden = YES;
			cell.imgView.hidden = YES;
		}
		
		return cell;
	}
	else 
	{
		SegDetailRowCellPad *cell = (SegDetailRowCellPad *)[tableView dequeueReusableCellWithIdentifier: CellIdentifier];
		if (cell == nil)  
		{
			NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"SegDetailRowCellPad" owner:self options:nil];
			for (id oneObject in nib)
				if ([oneObject isKindOfClass:[SegDetailRowCellPad class]])
					cell = (SegDetailRowCellPad *)oneObject;
		}
		
		// Configure the cell...
		
		Detail *d = details[row];
		cell.lblLabel.text = d.lbl;
		cell.lblValue.text = d.val;
		
		if(d.url != nil)
		{
			if(self.aDetails == nil)
				self.aDetails = [[NSMutableArray alloc] initWithObjects:nil];
			[aDetails addObject:d];
			cell.btnAction.tag = [aDetails count]-1;
			[cell.btnAction addTarget:self action:@selector(loadWebView:) forControlEvents:UIControlEventTouchUpInside];
			cell.lblValue.textColor = [UIColor blueColor];
		}
		else if(d.mapAddress != nil)
		{
			
			if(self.aDetails == nil)
				self.aDetails = [[NSMutableArray alloc] initWithObjects:nil];
			[aDetails addObject:d];
			cell.btnAction.tag = [aDetails count]-1;
			[cell.btnAction addTarget:self action:@selector(goSomeplace:) forControlEvents:UIControlEventTouchUpInside];
			cell.lblValue.textColor = [UIColor blueColor];
		}
		else {
			cell.lblValue.textColor = [UIColor blackColor];
		}

		
		if(d.imgDetail != nil)
			cell.iv.image = d.imgDetail;
		else 
			cell.iv.image = nil;
		
		//cell.textLabel.text = [NSString stringWithFormat:@"%@ - %@", d.lbl, d.val];
		
		return cell;
	}
}


/*
 // Override to support conditional editing of the table view.
 - (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
 // Return NO if you do not want the specified item to be editable.
 return YES;
 }
 */


/*
 // Override to support editing the table view.
 - (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
 
 if (editingStyle == UITableViewCellEditingStyleDelete) {
 // Delete the row from the data source
 [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:YES];
 }   
 else if (editingStyle == UITableViewCellEditingStyleInsert) {
 // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
 }   
 }
 */


/*
 // Override to support rearranging the table view.
 - (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath {
 }
 */


/*
 // Override to support conditional rearranging of the table view.
 - (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath {
 // Return NO if you do not want the item to be re-orderable.
 return YES;
 }
 */


-(void)goSomeplace:(NSString *)mapAddress VendorName:(NSString *)vendorName VendorCode:(NSString *)vendorCode
{
	MapViewController *mapView = [[MapViewController alloc] init];
	mapView.mapAddress = mapAddress;
	mapView.anoTitle = vendorName;
	mapView.anoSubTitle = mapAddress;
	//NSLog(@"mapAddress = %@", mapAddress);
	mapView.modalPresentationStyle = UIModalPresentationFormSheet;
	[dVC presentViewController:mapView animated:YES completion:nil];
	//[self presentViewController:mapView animated:YES completion:nil]; 
	
}


//-(void)callNumber:(NSString *)phoneNum
//{
//	//[[UIApplication sharedApplication] openURL:[NSURL URLWithString:[NSString stringWithFormat:@"tel://%@", phoneNum]]];
//}


-(IBAction)loadWebView:(NSString *)specialValueWeb WebViewTitle:(NSString *)webViewTitle
{
	
	//do web view
	WebViewController *webView = [[WebViewController alloc] init];
	webView.rootViewController = rootVC;
	webView.url = [NSString stringWithFormat:@"https://%@", specialValueWeb];
	webView.viewTitle = webViewTitle;
	webView.modalPresentationStyle = UIModalPresentationFormSheet;
	[dVC presentViewController:webView animated:YES completion:nil]; 
	
}

#pragma mark -
#pragma mark Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    // Navigation logic may go here. Create and push another view controller.
	/*
	 <#DetailViewController#> *detailViewController = [[<#DetailViewController#> alloc] initWithNibName:@"<#Nib name#>" bundle:nil];
     // ...
     // Pass the selected object to the new view controller.
	 [self.navigationController pushViewController:detailViewController animated:YES];
	 [detailViewController release];
	 */
	
	//NSUInteger section = [indexPath section];
    NSUInteger row = [indexPath row];
	
	//NSMutableArray *sectionData = [sections objectAtIndex:section];
	SegmentRow *segRow = details[row];
	
	if(![segRow isKindOfClass:[SegmentRow class]])
		return; // not a segment row so we cannot do anything here
	
	
	if(segRow.isWeb && segRow.isAirport)
	{
		NSString *url = [NSString stringWithFormat:@"gateguru://airports/%@", segRow.iataCode];
        [AppsUtil launchGateGuruAppWithUrl:url];
	}
	else 
		if(segRow.isWeb)
			[self loadWebView:segRow.url WebViewTitle:[NSString stringWithFormat:[Localizer getLocalizedText:@"IDV Web site for t"], segRow.viewTitle]];
		else if(segRow.isMap)
		{
			NSString *mapAddress = [SegmentData getMapAddress:segment.relStartLocation withLineBreaker:NO withDelimitor:YES];
			if ([mapAddress length])
			{
				//NSString *mapAddress = [NSString stringWithFormat:@"%@, %@, %@ %@", segment.startAddress, segment.startCity, segment.startState, segment.startPostalCode];
				[self goSomeplace:mapAddress VendorName:[NSString stringWithFormat:@"%@ %@", segment.vendorName, segment.segmentName] VendorCode:segment.vendor];
			} 
			else 
			{
                mapAddress = [NSString stringWithFormat:@"(%@) %@\n%@, %@", segment.relStartLocation.cityCode, segment.relStartLocation.airportName, segment.relStartLocation.airportCity, segment.relStartLocation.airportState];
				[self goSomeplace:mapAddress VendorName:[NSString stringWithFormat:@"%@ %@", segment.vendorName, segment.segmentName == nil? segment.relStartLocation.airportName : segment.segmentName] VendorCode:segment.vendor];
			}
		}
		else if (segRow.isPhone)
			[self callNumber:segment.phoneNumber];
	

	return;
	
}

#define kCellH 40
- (CGFloat)tableView:(UITableView *)tableView 
heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	int row = indexPath.row;
	
	if([segment.type isEqualToString:SEG_TYPE_AIR] && row == 0)
		return 80;
	else if([segment.type isEqualToString:SEG_TYPE_RAIL] && row == 1)
		return 114;
//	else if([segment.type isEqualToString:SEG_TYPE_DINING] && row == 1)
//		return 95;
	else 
		return kCellH;
}


#pragma mark -
#pragma mark Scroller Methods
//- (void)scrollViewDidScroll:(UIScrollView *)sender
//{
//	
//}

- (void)scrollViewDidEndScrollingAnimation:(UIScrollView *)newScrollView
{
//    CGFloat pageWidth = scroller.frame.size.width;
//    float fractionalPage = scroller.contentOffset.x / pageWidth;
//    NSInteger nearestNumber = lround(fractionalPage);
	
//    if (pager.pageIndex != nearestNumber)
//    {
////        PageViewController *swapController = currentPage;
////        currentPage = nextPage;
////        nextPage = swapController;
//    }
	
    //pager.currentPage = currentPage.pageIndex;
}

//- (void)setPageIndex:(NSInteger)newPageIndex
//{
//    pageIndex = newPageIndex;
//    
//    if (pageIndex >= 0 &&
//        pageIndex < [[DataSource sharedDataSource] numDataPages])
//    {
//        NSDictionary *pageData =
//		[[DataSource sharedDataSource] dataForPage:pageIndex];
//        label.text = [pageData objectForKey:@"pageName"];
//        textView.text = [pageData objectForKey:@"pageText"];
//    }
//}

//- (void)applyNewIndex:(NSInteger)newIndex pageController:(PageViewController *)pageController
//{
////    NSInteger pageCount = [[DataSource sharedDataSource] numDataPages];
////    BOOL outOfBounds = newIndex >= pageCount || newIndex < 0;
////	
////    if (!outOfBounds)
////    {
////        CGRect pageFrame = pageController.view.frame;
////        pageFrame.origin.y = 0;
////        pageFrame.origin.x = scrollView.frame.size.width * newIndex;
////        pageController.view.frame = pageFrame;
////    }
////    else
////    {
////        CGRect pageFrame = pageController.view.frame;
////        pageFrame.origin.y = scrollView.frame.size.height;
////        pageController.view.frame = pageFrame;
////    }
////	
////    pageController.pageIndex = newIndex;
//}

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
	
	//NSLog(@"imageArray count = %d", [imageArray count]);
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
	//parentVC.pagePos = page;
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


@end


@implementation scrollingIV2
@synthesize parentVC, imageArray, pos;

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
	UITouch *touch = [[event allTouches] anyObject];
	if ([touch tapCount] == 2)
	{
		ImageViewerVC *vc = [[ImageViewerVC alloc] init];
		vc.imageArray = (NSMutableArray *)imageArray;
		//vc.pagePos = parentVC.pagePos;
		vc.modalPresentationStyle = UIModalPresentationFormSheet;
		[parentVC presentViewController:vc animated:YES completion:nil];
		[self.nextResponder touchesBegan:touches withEvent:event];
		
		//		if(self.zoomScale > 1.0){
		//			[self setZoomScale:1 animated:YES];
		//		}else {
		//			[self setZoomScale:3 animated:YES];
		//		}
	}
}

@end

@implementation scrolling2

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
	UITouch *touch = [[event allTouches] anyObject];
	if ([touch tapCount] == 2)
	{
		if(self.zoomScale > 1.0){
			[self setZoomScale:1 animated:YES];
		}else {
			[self setZoomScale:3 animated:YES];
		}
	}
}
@end
