//
//  HotelBenchmarkResultsVC.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 20/01/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelBenchmarkResultsVC.h"
#import "HotelBenchmark.h"
#import "HotelBenchmarkCell.h"
#import "UserConfig.h"

@interface HotelBenchmarkResultsVC ()

@end

@implementation HotelBenchmarkResultsVC

- (instancetype)initWithTitle:(NSString *)title
{
    HotelBenchmarkResultsVC *vc = [[UIStoryboard storyboardWithName:@"TravelPoints" bundle:nil] instantiateViewControllerWithIdentifier:@"HotelBenchmarkResultsVC"];
    vc.navigationItem.title = title;
    return vc;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    if ([self respondsToSelector:@selector(setEdgesForExtendedLayout:)]) {// If iOS 7
        self.edgesForExtendedLayout = UIRectEdgeNone;
    }
    // Resize header label according to Attributed string size. sizeToFit doesn;t work properly with attributed strings
    self.lblHeader.attributedText = [self getHeaderAttributedText];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [self.benchmarksList count];
}

- (void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self.lblHeader sizeToFit];
    CGFloat tableHeightOffset = self.lblHeader.frame.origin.y + self.lblHeader.frame.size.height + 8 - self.tableView.frame.origin.y;
    self.tableView.frame = CGRectMake(self.tableView.frame.origin.x, self.lblHeader.frame.origin.y + self.lblHeader.frame.size.height + 8, self.tableView.frame.size.width, self.tableView.frame.size.height - tableHeightOffset);
}

- (UITableViewCell*)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    HotelBenchmarkCell *cell = [tableView dequeueReusableCellWithIdentifier:@"HotelBenchmarkCell"];
    if (!cell) {
        cell = [[HotelBenchmarkCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"HotelBenchmarkCell"];
    }
    HotelBenchmark *benchmark = self.benchmarksList[indexPath.row];
    if (benchmark.price) {
        cell.lblPrice.textColor = [UIColor blackColor];
        cell.lblPrice.text = [FormatUtils formatMoneyWithNumber:@(benchmark.price) crnCode:benchmark.currency];
        cell.lblPerNight.hidden = NO;
        cell.lblPerNight.text = [@"per night" localize];
    }
    else {
        cell.lblPrice.textColor = [UIColor grayColor];
        cell.lblPrice.text = [@"Unavailable" localize];
        cell.lblPerNight.hidden = YES;
    }
    if ([benchmark.name length] && [benchmark.subdivCode length])
        cell.lblLocationName.text = [NSString stringWithFormat:@"%@, %@",benchmark.name, benchmark.subdivCode];
    else
        cell.lblLocationName.text = benchmark.name;
       
    return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section
{
    return 1;
}

-(NSAttributedString *)getHeaderAttributedText
{
    NSMutableAttributedString *text = [[NSMutableAttributedString alloc] init];
    NSDictionary *attributes = @{NSFontAttributeName : [UIFont fontWithName:@"HelveticaNeue" size:14], NSForegroundColorAttributeName : [UIColor blackColor]};
    //NSDictionary *newlineAttributes = @{NSFontAttributeName : [UIFont fontWithName:@"HelveticaNeue" size:8], NSForegroundColorAttributeName : [UIColor blackColor]};
    NSDictionary *headingAttributes = @{NSFontAttributeName : [UIFont boldSystemFontOfSize:14], NSForegroundColorAttributeName : [UIColor blackColor]};
    
    if ([self.headerText length]) {
        [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@\n\n",self.headerText] attributes:attributes]];
    }
    else {
        [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@\n\n",[self getConfigBasedInitialMessage]] attributes:attributes]];
        
        [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@:",[@"Location" localize]] attributes:headingAttributes]];
        [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@" %@\n",self.searchLocation] attributes:attributes]];
        [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@:",[@"Month of stay" localize]] attributes:headingAttributes]];
        [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@" %@\n",self.monthOfStayString] attributes:attributes]];
        [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@:", [@"Distance" localize]] attributes:headingAttributes]];
        [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@" %@\n\n",self.distanceString] attributes:attributes]];
    }
    NSString *footerText = [@"Hotel Price to Beat" localize];// stringByAppendingFormat:@" (%@)",[@"per night" localize]];
    footerText = [footerText uppercaseStringWithLocale:[NSLocale currentLocale]];
    [text appendAttributedString:[[NSAttributedString alloc] initWithString:footerText attributes:attributes]];
    
    return text;
}

-(NSString *)getConfigBasedInitialMessage
{
    if([[UserConfig getSingleton].travelPointsConfig[@"HotelTravelPointsEnabled"] boolValue])
        return [@"PRICE_TO_BEAT_HOTEL_RESULTS_HEADER" localize];
    else
        return [@"PRICE_TO_BEAT_DISABLED_HOTEL_RESULTS_HEADER" localize];
}

@end
