//
//  TripViolationSummaryVC.m
//  ConcurMobile
//
//  Created by Deepanshu Jain on 17/07/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "TripViolationSummaryVC.h"
#import "ViolationCell.h"

@interface TripViolationSummaryVC ()
@property (strong, nonatomic) IBOutlet UILabel *lblName;
@property (strong, nonatomic) IBOutlet UILabel *lblDate;
@property (strong, nonatomic) IBOutlet UILabel *lblAmount;
@property (strong, nonatomic) IBOutlet UILabel *lblBottom;

@property (strong, nonatomic) NSArray *violations;

@end

@implementation TripViolationSummaryVC

-(void)initHeader
{
    self.lblName.text = self.lblNameText;
    self.lblDate.text = self.lblDateText;
    self.lblAmount.text = self.lblAmountText;
    self.lblBottom.text = self.lblBottomText;
}

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
    self.tableView = [[UITableView alloc] initWithFrame:CGRectZero style:UITableViewStylePlain];
    // Do any additional setup after loading the view from its nib.
    [self.tableView registerNib:[UINib nibWithNibName:@"ViolationCell" bundle:nil] forCellReuseIdentifier:@"ViolationCell"];
}

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self initHeader];
    [self setToolbarItems:nil];
    [self setTitle:[@"Violation Summary" localize]];
    self.violations = [self.trip.relViolation allObjects];
    [self bringAllItinViolationsToTop];
    
    [Flurry logEvent:@"Trip Approvals: Violation Summary viewed"];
}

-(void)bringAllItinViolationsToTop
{
    NSMutableArray *itinViolations = [[NSMutableArray alloc] init];
    NSMutableArray *otherViolations = [[NSMutableArray alloc] init];
    
    for (EntityViolation *violation in self.violations) {
        if ([violation.type isEqualToString:@"Itinerary"])
            [itinViolations addObject:violation];
        else
            [otherViolations addObject:violation];
    }
    [itinViolations addObjectsFromArray:[otherViolations copy]];
    self.violations = [itinViolations copy];
}


- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)viewDidUnload {
    [self setLblName:nil];
    [self setLblDate:nil];
    [self setLblAmount:nil];
    [self setLblBottom:nil];
    [super viewDidUnload];
}

#pragma TableView Data source

-(NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    return [self.violations count];
}

-(NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    EntityViolation *violation = self.violations[section];
    return [self getHeaderStringOnViolationType:violation.type];
}

-(NSInteger) tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return 1;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    EntityViolation *violation = self.violations[indexPath.section];
    return [[self getViolationAttributedText:violation] boundingRectWithSize:CGSizeMake(260, 10000) options:NSStringDrawingUsesLineFragmentOrigin | NSStringDrawingUsesFontLeading context:nil].size.height;
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    EntityViolation *violation = self.violations[indexPath.section];
    ViolationCell *cell = (ViolationCell*)[self.tableView dequeueReusableCellWithIdentifier:@"ViolationCell"];
    cell.lblViolationText.attributedText = [self getViolationAttributedText:violation];
    return cell;
}

-(NSAttributedString *)getViolationAttributedText:(EntityViolation*)violation
{
    NSMutableAttributedString *text = [[NSMutableAttributedString alloc] init];
    NSDictionary *attributes = @{NSFontAttributeName : [UIFont fontWithName:@"HelveticaNeue" size:12], NSForegroundColorAttributeName : [UIColor blackColor]};
    NSDictionary *newlineAttributes = @{NSFontAttributeName : [UIFont fontWithName:@"HelveticaNeue" size:8], NSForegroundColorAttributeName : [UIColor blackColor]};
    NSDictionary *headingAttributes = @{NSFontAttributeName : [UIFont boldSystemFontOfSize:12], NSForegroundColorAttributeName : [UIColor blackColor]};
    
    [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@\n",(violation.rule ? violation.rule : @"")] attributes:attributes]];
    
    if (![violation.type isEqualToString:@"Itinerary"] || (violation.reason && [violation.reason length]) ) {
        [text appendAttributedString:[[NSAttributedString alloc] initWithString:@"\n" attributes:newlineAttributes]];
        [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@\n",[@"Violation Reason Code" localize]] attributes:headingAttributes]];
        [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@\n",violation.reason] attributes:attributes]];
    }
    if (![violation.type isEqualToString:@"Itinerary"] || (violation.comments && [violation.comments length]) ) {
        [text appendAttributedString:[[NSAttributedString alloc] initWithString:@"\n" attributes:newlineAttributes]];
        [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@\n",[@"Booker Comments" localize]] attributes:headingAttributes]];
        [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@\n",violation.comments] attributes:attributes]];
    }
    if (![violation.type isEqualToString:@"Itinerary"] || (violation.cost && [violation.cost length]) || (violation.costAdditionalInfo && [violation.costAdditionalInfo length]) ) {
        [text appendAttributedString:[[NSAttributedString alloc] initWithString:@"\n" attributes:newlineAttributes]];
        [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@\n",[self getCostStringOnViolationType:violation.type]] attributes:headingAttributes]];
        if (violation.cost && [violation.cost length])
            [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@\n",violation.cost] attributes:attributes]];
        if (violation.costAdditionalInfo && [violation.costAdditionalInfo length])
            [text appendAttributedString:[[NSAttributedString alloc] initWithString:[NSString stringWithFormat:@"%@\n",violation.costAdditionalInfo] attributes:attributes]];
    }
    
    return text;
}

-(NSString*)getCostStringOnViolationType:(NSString*)type
{
    if ([type isEqualToString:@"Car"])
        return [@"Daily Rate" localize];
    else if ([type isEqualToString:@"Hotel"])
        return [@"ITIN_DETAILS_VIEW_RATE" localize]; // has value => @"Rate";
    else if ([type isEqualToString:@"Air"])
        return [@"Airfare quoted total" localize];
    else
        return [@"ITIN_DETAILS_VIEW_RATE" localize]; // has value => @"Rate";
}


-(NSString*)getHeaderStringOnViolationType:(NSString*)type
{
    if ([type isEqualToString:@"Car"])
        return [@"Car Rule Violation" localize];
    else if ([type isEqualToString:@"Hotel"])
        return [@"Hotel Rule Violation" localize]; // has value => @"Rate";
    else if ([type isEqualToString:@"Air"])
        return [@"Flight Rule Violation" localize];
    else if ([type isEqualToString:@"Itinerary"])
        return [@"Itinerary Rule Violation" localize];
    else if ([type isEqualToString:@"Rail"])
        return [@"Rail Rule Violation" localize];
    else
        return type;
}


@end





