//
//  FFFormController.m
//  ConcurMobile
//
//  Created by laurent mery on 26/10/2014.
//  Copyright (c) 2014 concur. All rights reserved.
//

#import "FFFormController-private.h"



@implementation FFFormController

#pragma mark - init

//public
-(id)initWithTableView:(UITableView*)tableViewForm andDelegate:(id)delegate{
	
	if (self = [super init]){
		
		_sections = [[NSMutableArray alloc]init];
		
        
        //delegate
        _delegateVC = delegate;
        
        //tableView
		_tableViewForm = tableViewForm;
		_tableViewForm.delegate = self;
		_tableViewForm.dataSource = self;
        //each cell will managed it's line separator
        [_tableViewForm setSeparatorStyle:UITableViewCellSeparatorStyleNone];

        //init
        _tableViewFormOriginalRect = tableViewForm.frame;
        
        /*
         * register all cell's layout type
         * cell layout type correspond to a specific cell design
         * we have
         - static cell (Read Only text)
         - Text cell (Read/Write text)
         - Connected List cell...
         */
        [_tableViewForm registerClass:[FFStaticCell class] forCellReuseIdentifier:FFCellReuseIdentifierStatic];
        [_tableViewForm registerClass:[FFTextCell class] forCellReuseIdentifier:FFCellReuseIdentifierText];
        [_tableViewForm registerClass:[FFTextAreaCell class] forCellReuseIdentifier:FFCellReuseIdentifierTextArea];
        [_tableViewForm registerClass:[FFDateCell class] forCellReuseIdentifier:FFCellReuseIdentifierDate];
        [_tableViewForm registerClass:[FFNumberCell class] forCellReuseIdentifier:FFCellReuseIdentifierNumber];
        [_tableViewForm registerClass:[FFMoneyCell class] forCellReuseIdentifier:FFCellReuseIdentifierMoney];
	}
	return self;
}


#pragma mark - Cells



-(NSString *)determineCellReuseIdentifierAtIndexPath:(NSIndexPath *)indexPath{
    
    FFField *field = [self fieldAtIndexPath:indexPath];

    NSString *fieldType;
    
    if ([field isTextLayout]){
        
        fieldType = FFCellReuseIdentifierText;
    }
    
    else if ([field isTextAreaLayout]){
        
        fieldType = FFCellReuseIdentifierTextArea;
    }
    
    else if ([field isDateLayout]){
        
        fieldType = FFCellReuseIdentifierDate;
    }
    
    else if ([field isNumberLayout]){
        
        fieldType = FFCellReuseIdentifierNumber;
    }
    
    else if ([field isMoneyLayout]){
        
        fieldType = FFCellReuseIdentifierMoney;
    }
    
    else {
        
        fieldType = FFCellReuseIdentifierStatic;
    }
	
	return fieldType;
}


#pragma mark - Form and Fields

//public
-(void)addForm:(NSString*)formName withFormID:(NSString*)formID isEditable:(BOOL)isEditable{
    
    
    //cache form
    static NSMutableDictionary *forms;
    if (forms == nil) {
        
        forms = [[NSMutableDictionary alloc]init];
    }
    
    CTEFormFields *form = [forms objectForKey:formID];
    
    if(form == nil){
        
        form = [[CTEFormFields alloc] init];
        [form formbyID:formID];
        [forms setObject:form forKey:formID];
    }
    
    //create a section
    FFSection *newSection = [[FFSection alloc]init];
    
    [newSection setName:formName];
    [newSection setForm:form];
    [newSection setIsFormEditable:isEditable];
    
    [_sections addObject:newSection];
    
    //create section.fields
    [self initFieldsInSection:newSection];
    
    
}

-(void)initFieldsInSection:(FFSection*)section{
    

    //create FFFields
    NSMutableArray *ffFields = [[NSMutableArray alloc]init];
    
    NSArray *cteFields = [section.form fieldsOrdered];
    for (CTEField *cteField in cteFields) {

        CTEDataTypes *dataType = [self dataTypeForFfFieldLight:[[FFFieldLight alloc]initWithDef:cteField andDelegate:self]];
        
        [ffFields addObject:[[FFField alloc]initWithDef:cteField andDataTypes:dataType andDelegate:self]];
    }
    
    NSArray *fields;
    fields = [self fieldsDisplayFilteredByFields:ffFields withinSection:section];

    [section setFields:fields];
}

//public
-(CTEDataTypes*)dataTypeForFfFieldLight:(FFFieldLight*)ffFieldLight{
    
    //prototype doesn't have access to data object
    return [[CTEDataTypes alloc]init];
}

-(NSArray*)fieldsDisplayFilteredByFields:(NSArray*)fields withinSection:(FFSection*)section{
    
    //Update Access with global form information
    for (FFField *field in fields){
        
        if(!section.isFormEditable){
            
            [field setReadOnlyMax];
        }
        
        NSString *access = [self accessForField:field];
        [field setAccess:access];
    }
    
    //remove hidden fields
    NSMutableArray *fieldsFiltered = [NSMutableArray arrayWithArray:fields];
    NSMutableArray *fieldsDiscard = [[NSMutableArray alloc]init];
    
    for (FFField *field in fieldsFiltered){
        
        if ([field isAccessHD]){
            
            [fieldsDiscard addObject:field];
        }
    }
    [fieldsFiltered removeObjectsInArray:[fieldsDiscard copy]];
    
    return [fieldsFiltered copy];
}

//public
-(NSString*)accessForField:(FFField*)field{
    
    if ([field isAccessRO]) {
        
        return @"RO";
    }
    else if ([field isAccessRW]){
        
        return @"RW";
    }
    return @"HD";
}


//public
-(NSArray*)fieldsOrderedInSection:(FFSection*)section{
    
    NSArray *fields = section.fields;
    
    if (fields == nil) {
        
        fields = [section.form fieldsOrdered];
    }
    
    return fields;
}


-(FFSection*)sectionByName:(NSString*)sectionName{
	
	FFSection *sectionToReturn;
	
	for (FFSection *section in _sections){
		
		if (section.name == sectionName){
			
			return section;
		}
	}
	return sectionToReturn;
}

-(NSInteger)sectionIndexByName:(NSString*)sectionName{
    
    NSInteger indexSection;
    
    //found section index
    for(indexSection = 0; indexSection < [self.sections count]; indexSection++){
        
        FFSection *section = [self.sections objectAtIndex:indexSection];
        if ([section.name isEqualToString:sectionName]){
            
            return indexSection;
        }
    }
    return indexSection;
}

//public
-(FFField*)fieldAtIndexPath:(NSIndexPath*)indexPath{
	
	FFField *field = [[self fieldsForSectionAtIndex:indexPath.section] objectAtIndex:indexPath.row];
	return field;
}

-(NSArray*)fieldsForSectionAtIndex:(NSInteger)indexSection{
    
    NSArray *fields;
    
    if ([_sections count] > 0) {
        
        FFSection *section = [_sections objectAtIndex:indexSection];
        return section.fields;
    }
    
    return fields;
}



-(BOOL)isDirtyAtIndexPath:(NSIndexPath*)indexPath{
    
    FFField *field = [self fieldAtIndexPath:indexPath];
    
    return [field.dataType isDirty];
}

-(BOOL)isDirtyForm:(NSString*)formName{
    
    BOOL isDirty = NO;

    NSInteger sectionIndex = [self sectionIndexByName:formName];
    FFSection *section = [self.sections objectAtIndex:sectionIndex];
    NSIndexPath *indexPath;
    
    //found field index
    for(int x = 0; x < [section.fields count]; x++){
    
        indexPath = [NSIndexPath indexPathForRow:x inSection:sectionIndex];
        isDirty = isDirty || [self isDirtyAtIndexPath:indexPath];
        
        if (isDirty){
            
            break;
        }
    }
    
    return isDirty;
}


-(BOOL)isDirtyAllForms{
    
    BOOL isDirty = NO;
    FFSection *section;
    int x;
    
    //found section index
    for(x = 0; x < [self.sections count]; x++){
        
        section = [self.sections objectAtIndex:x];
        isDirty = isDirty || [self isDirtyForm:section.name];
        
        if (isDirty){
            
            break;
        }
    }
    
    return isDirty;
}


-(void)resetAtIndexPath:(NSIndexPath*)indexPath{
    
    FFField *field = [self fieldAtIndexPath:indexPath];
    [field.dataType reset];
}


-(void)resetForm:(NSString*)formName{
    
    NSInteger sectionIndex = [self sectionIndexByName:formName];
    FFSection *section = [self.sections objectAtIndex:sectionIndex];
    NSIndexPath *indexPath;
    
    //found field index
    for(int x = 0; x < [section.fields count]; x++){
        
        indexPath = [NSIndexPath indexPathForRow:x inSection:sectionIndex];
        [self resetAtIndexPath:indexPath];
    }
}


-(void)resetAllForms{
    
    FFSection *section;
    int x;
    
    //found section index
    for(x = 0; x < [self.sections count]; x++){
        
        section = [self.sections objectAtIndex:x];
        [self resetForm:section.name];
    }
}


#pragma mark - form Validation (#FFCellDelegateProtocol)

//#protocol FFCellDelegateProtocol
-(NSArray*)errorsOnValidateField:(FFField*)field{
    
    NSArray *errors = [[NSArray alloc] init];
    return errors;
}

#pragma mark - table view


-(FFBaseCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{

	FFField *field = [self fieldAtIndexPath:indexPath];
	FFBaseCell *cell;

	cell = [tableView dequeueReusableCellWithIdentifier:[self determineCellReuseIdentifierAtIndexPath:indexPath]];
    [cell initWithField:field andDelegate:self];
    [cell setIndexPath:indexPath];
	
	return cell;
}


-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)sectionIndex{
	
	return [[self fieldsForSectionAtIndex:sectionIndex] count];
}


-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath{
	
	CGFloat height;
	
	FFBaseCell *cell = (FFBaseCell*)[self tableView:tableView cellForRowAtIndexPath:indexPath];
	
	height = [cell heightView];
	
	return height;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section{
	
	return 0.1;
}


- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section{
	
	return 0.1;
}


-(void)refreshAtIndexPath:(NSIndexPath *)indexPath{
	
	[_tableViewForm reloadRowsAtIndexPaths:@[indexPath] withRowAnimation:NO];
}


#pragma mark - cell

//#protocol FFFormProtocol
-(NSString*)iconLabelForField:(FFField*)field{
    
    NSString *iconLabel = @"";
    return iconLabel;
}



//#protocol FFCellDelegateProtocol
-(CGFloat)heightRowConfigTableViewForm{

    //TODO: found correct property : tableview.rowHeight
    return 62.0;
}


#pragma mark - Delegate editor


//#protocol FFCellDelegateProtocol.h
-(void)pushExternalEditorVC:(UIViewController*)vc fromIndexPath:(NSIndexPath*)indexPath{
    
    //scroll to edited cell
    [self setIsPendingExternalEdited:YES];
    
    [_delegateVC viewEndEditing];
    
    [_delegateVC pushEditViewController:vc];
}


//public
-(void)refreshAll{
    
    [self.tableViewForm reloadData];
}

//public
-(void)refreshCurrentEditedCell{
    
    NSIndexPath *indexPath = [self.tableViewForm indexPathForSelectedRow];
    [self refreshAtIndexPath:indexPath];
    
    [self setIsPendingExternalEdited:NO];
}

//#protocol FFCellDelegateProtocol
-(void)onKeyboardUpWithSize:(CGFloat)heightKeyBoard ScrollToIndexPath:(NSIndexPath *)indexPath{

    
    //resize display area
    CGFloat viewHeight = [[UIScreen mainScreen] bounds].size.height;
    CGFloat tableViewFormY = self.tableViewFormOriginalRect.origin.y;
    CGFloat tableViewFormHeight = self.tableViewFormOriginalRect.size.height;
    
    CGFloat bottomScreen2bottomTableView = viewHeight - (tableViewFormY + tableViewFormHeight);
    CGFloat newTableViewFormHeight = tableViewFormHeight - (heightKeyBoard - bottomScreen2bottomTableView);
    
    [UIView animateWithDuration:0.25 animations:^{
        
        CGRect frame = self.tableViewForm.frame;
        frame.size.height = newTableViewFormHeight;
        self.tableViewForm.frame = frame;
    }];
    
    //scroll to edited cell
    [self.tableViewForm selectRowAtIndexPath:indexPath animated:NO scrollPosition:UITableViewScrollPositionTop];
}


//#protocol FFCellDelegateProtocol
-(void)onKeyboardDownFromIndexPath:(NSIndexPath *)indexPath{
    
    //reallocate full area
    [UIView animateWithDuration:0.25 animations:^{

        self.tableViewForm.frame = self.tableViewFormOriginalRect;
    }];
}


#pragma mark - Memory management


-(void)dealloc{
	
	_tableViewForm.delegate = nil;
	_tableViewForm.dataSource = nil;
	_tableViewForm = nil;
	
	_delegateVC = nil;
}

@end


